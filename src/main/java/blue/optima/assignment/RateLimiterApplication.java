package blue.optima.assignment;

import blue.optima.assignment.cache.HazCacheService;
import blue.optima.assignment.mapper.MappingTableRowMapper;
import blue.optima.assignment.model.MappingTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@SpringBootApplication
public class RateLimiterApplication implements CommandLineRunner {
    @Autowired
    private Environment env;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    HazCacheService hazCacheService;


    public static void main(final String[] args) {
        SpringApplication.run(RateLimiterApplication.class, args);
    }

    @Override
    public void run(final String... args) throws Exception {
        List<String> userList = Arrays.asList(env.getProperty("rate.limit.api.users").split(","));
        List<String> apiList = Arrays.asList(env.getProperty("rate.limit.api.apis").split(","));
        int apiLimit = Integer.parseInt(env.getProperty("default.rate.limit"));
        List<Integer> apiLimitList = new ArrayList<>();
        if (env.getProperty("rate.limit.api.limit") != null) {
            String limitString = env.getProperty("rate.limit.api.limit");
            apiLimitList = Arrays.stream(limitString.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        }
        String sql = "SELECT * FROM mapping_table where user_name = ? and api = ?";
        String dbSql = "SELECT * FROM mapping_table";
        String updateSql = "INSERT into mapping_table(user_name, api, rate_limit) values(?,?,?)";
        List<Integer> finalApiLimitList = apiLimitList;
        AtomicInteger count = new AtomicInteger(0);
        List<MappingTable> dbMappings = jdbcTemplate.query(dbSql,
                new MappingTableRowMapper());
        AtomicInteger idx = new AtomicInteger(0);
        userList.forEach((userName) -> {
            int currentApiLimit = apiLimit;
            if (finalApiLimitList.size() > 0) {
                currentApiLimit = finalApiLimitList.get(count.getAndIncrement());
            }
            String api = apiList.get(idx.getAndIncrement());
            dbMappings.removeIf(mappingTable -> mappingTable.getUserName().equals(userName) && mappingTable.getApi().equals(api));
            List<MappingTable> mappings = jdbcTemplate.query(sql,
                    new MappingTableRowMapper(), userName, api);
            if (mappings.size() == 0) {
                jdbcTemplate.update(updateSql, userName, api, currentApiLimit);
                hazCacheService.resolveBucket(userName + "-" + api, currentApiLimit);
                System.out.println("New mapping username-> " + userName + " api -> " + api + " limit-> " + currentApiLimit);
            } else {
                MappingTable mappingTable = mappings.get(0);
                hazCacheService.resolveBucket(userName + "-" + api, mappingTable.getRateLimit());
                System.out.println("Existing mapping username-> " + mappingTable.getUserName() + " api -> " + mappingTable.getApi() + " limit-> " + mappingTable.getRateLimit());
            }
        });
        dbMappings.forEach((MappingTable mappingTable)->{
            hazCacheService.resolveBucket(mappingTable.getUserName() + "-" + mappingTable.getApi(), mappingTable.getRateLimit());
            System.out.println("Existing db mapping username-> " + mappingTable.getUserName() + " api -> " + mappingTable.getApi() + " limit-> " + mappingTable.getRateLimit());
        });
    }

}

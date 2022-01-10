package blue.optima.assignment.apis;

import blue.optima.assignment.cache.HazCacheService;
import blue.optima.assignment.mapper.MappingTableRowMapper;
import blue.optima.assignment.model.MappingTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestController {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    HazCacheService hazCacheService;

    @GetMapping("/get/api1")
    String api1() {
        return "api1";
    }

    @GetMapping("/get/api2")
    String api2() {
        return "api2";
    }

    @GetMapping("/get/api3")
    String api3() {
        return "api2";
    }

    @GetMapping("/get/api4")
    String api4() {
        return "api2";
    }

    @GetMapping("check")
    String api5() {
        hazCacheService.resolveBucket("1111111",4);
        return "api5";
    }

    @GetMapping("check2")
    String api6() {
        System.out.println(hazCacheService.getCache().keySet());
        return "api2";
    }



    @GetMapping("/update")
    String updateRateLimit(String user, String api, Integer limit) {
        System.out.println(".............");
        String sql = "SELECT * FROM mapping_table where user_name = ? and api = ?";
        String update = "UPDATE mapping_table set rate_limit = ? where user_name = ? and api = ?";
        List<MappingTable> mappings = jdbcTemplate.query(sql,
                new MappingTableRowMapper(), user, api);
        String returnString;
        if(user == null || api == null || limit == null){
            returnString = "Please pass parameters user, api, limit";
        } else if(limit <= 0){
            returnString = "Limit value should be greater than 0.";
        } else if (mappings.size() > 0) {
            jdbcTemplate.update(update, limit, user, api);
            hazCacheService.updateBucket(user+"-"+api, limit);
            System.out.println(hazCacheService.getCache().keySet());
            System.out.println(hazCacheService.getCache().get("user1-api1"));
            returnString = "Update rate limit to " + limit + " for user " + user + " and api " + api;
        } else {
            returnString = "Mapping not found for user " + user + " and api " + api;
        }
        return returnString;
    }
}

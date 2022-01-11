package blue.optima.assignment.apis;

import blue.optima.assignment.cache.HazCacheService;
import blue.optima.assignment.cache.JsonSearialzer;
import blue.optima.assignment.mapper.MappingTableRowMapper;
import blue.optima.assignment.model.MappingTable;
import com.google.common.base.Optional;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.EstimationProbe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
public class TestController {
    @Autowired
    private Environment env;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    HazCacheService hazCacheService;

    @GetMapping("/get/api1")
    String api1() {
        return "Success";
    }

    @GetMapping("/get/api2")
    String api2() {
        return "Success";
    }

    @GetMapping("/get/api3")
    String api3() {
        return "Success";
    }

    @GetMapping("/get/api4")
    String api4() {
        return "Success";
    }

    @GetMapping("/api5")
    String api5() {
        return "Success";
    }

    @GetMapping("/check")
    String api6() {
        System.out.println(hazCacheService.getCache().keySet());
        return "Success";
    }


    @GetMapping("/update")
    String updateRateLimit(String user, String api, Integer limit) {
        String sql = "SELECT * FROM mapping_table where user_name = ? and api = ?";
        String update = "UPDATE mapping_table set rate_limit = ? where user_name = ? and api = ?";
        List<MappingTable> mappings = jdbcTemplate.query(sql,
                new MappingTableRowMapper(), user, api);
        String returnString;
        if (user == null || api == null || limit == null) {
            returnString = "Please pass parameters user, api, limit";
        } else if (limit <= 0) {
            returnString = "Limit value should be greater than 0.";
        } else if (mappings.size() > 0) {
            jdbcTemplate.update(update, limit, user, api);
            hazCacheService.updateBucket(user + "-" + api, limit);
            returnString = "Update rate limit to " + limit + " for user " + user + " and api " + api;
        } else {
            returnString = "Mapping not found for user " + user + " and api " + api;
        }
        return returnString;
    }

    @RequestMapping(value = "/getAllUserApiLimit", headers = "Content-Type=application/x-www-form-urlencoded", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<String> getAllUserApiLimit(HttpServletRequest request,
                                              HttpServletResponse response, Model model) {
        String jsonString = request.getParameter("data");
        JsonSearialzer jsonSearialzer = new JsonSearialzer();
        Optional<String[]> optional = jsonSearialzer.toObject(jsonString, String[].class);
        List<String> apiList = new ArrayList<>();
        if (optional.isPresent()) {
            apiList = Arrays.asList(optional.get());
        }
        Map<String, String> restMap = new HashMap<>();
        apiList.forEach((apiKey) -> {
            Bucket bucket = hazCacheService.resolveBucket(apiKey, Integer.parseInt(env.getProperty("default.rate.limit")));
            EstimationProbe probe = bucket.estimateAbilityToConsume(1);
            long tokens = probe.getRemainingTokens();
            long time = probe.getNanosToWaitForRefill() / (1000 * 1000 * 1000);
            if (tokens > 0) {
                restMap.put(apiKey, "Can access available tokens " + tokens);
            } else {
                restMap.put(apiKey, "Can not access" + " for next " + time + " seconds");
            }
        });
        Optional<String> stringOptional = jsonSearialzer.toJson(restMap);
        String finalStr = stringOptional.isPresent() ? stringOptional.get() : "[]";
        return ResponseEntity.ok()
                .body(finalStr);

    }


    @RequestMapping(value = "/deleteBucket", headers = "Content-Type=application/x-www-form-urlencoded", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<String> deleteBucket(HttpServletRequest request,
                                        HttpServletResponse response, Model model) {
        String apiKey = request.getParameter("data");
        hazCacheService.delete(apiKey);
        return ResponseEntity.ok()
                .body("Successfully deleted");

    }

    @RequestMapping(value = "/createBucket", headers = "Content-Type=application/x-www-form-urlencoded", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<String> createBucket(HttpServletRequest request,
                                        HttpServletResponse response, Model model) {
        String[] apiKey = request.getParameter("data").split(",");
        hazCacheService.resolveBucket(apiKey[0], Integer.parseInt(apiKey[1]));
        return ResponseEntity.ok()
                .body("Successfully deleted");

    }

    @RequestMapping(value = "/updateBucket", headers = "Content-Type=application/x-www-form-urlencoded", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<String> updateBucket(HttpServletRequest request,
                                        HttpServletResponse response, Model model) {
        String[] apiKey = request.getParameter("data").split(",");
        hazCacheService.updateBucket(apiKey[0], Integer.parseInt(apiKey[1]));
        return ResponseEntity.ok()
                .body("Successfully deleted");

    }
}

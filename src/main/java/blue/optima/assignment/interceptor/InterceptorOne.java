package blue.optima.assignment.interceptor;

import blue.optima.assignment.cache.HazCacheService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

@Component
public class InterceptorOne implements HandlerInterceptor {
    @Autowired
    private Environment env;
    @Autowired
    private HazCacheService hazCacheService;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        List<String> userList = Arrays.asList(env.getProperty("rate.limit.api.users").split(","));
        if(httpServletRequest.getParameterMap().get("user") == null){
            httpServletResponse.getOutputStream().print("Please pass user parameter to get access to api.");
            return false;
        } else if(!userList.contains(httpServletRequest.getParameterMap().get("user")[0])){
            httpServletResponse.getOutputStream().print("Unauthorized access.");
            return false;
        } else {
            String userId = httpServletRequest.getParameterMap().get("user")[0];
            String uri = httpServletRequest.getRequestURI();
            int idx = uri.lastIndexOf("/");
            String apiKey = userId+"-"+uri.substring(idx+1);
            Bucket bucket = hazCacheService.resolveBucket(apiKey, 1);
            ConsumptionProbe consumptionProbe = bucket.tryConsumeAndReturnRemaining(1);
            if (consumptionProbe.isConsumed()) {
                return true;
            } else {
                httpServletResponse.getOutputStream().print("Rate limit exceeded wait for "+ (consumptionProbe.getNanosToWaitForRefill()/(1000*1000*1000))+ " seconds.");
                return false;
            }
        }
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}

package cn.itlou.check.api;

import cn.itlou.check.core.ScheduleCheckInTask;
import cn.itlou.check.entity.UserInfo;
import cn.itlou.check.repo.UserInfoRepository;
import cn.itlou.check.util.TokenUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Optional;

@Slf4j
@RestController
public class CheckInformationApi {

    @Resource
    UserInfoRepository userInfoRepository;

    @GetMapping("/checkTokenTime")
    public String checkTokenTime(@RequestParam("name") String name) {
        Optional<UserInfo> userInfoOptional = userInfoRepository.findById(name);
        if (userInfoOptional.isPresent()) {
            return parseTime(userInfoOptional.get());
        } else {
            return "不存在该用户!";
        }
    }

    private String parseTime(UserInfo userInfo) {
        JSONObject token2Json = TokenUtil.parseToken2Json(userInfo.getToken());
        Long exp = token2Json.getLong("exp");
        Long current = System.currentTimeMillis() / 1000;
        long minuites = (current - exp) / 3600 % 60;
        long hours = (current - exp) / 3600;
        if (current - exp >= 0) {
            return "您的token已过期" + hours + "小时" + minuites + "分钟......";
        }
        return "您的token将于" + -hours + "小时" + -minuites + "分后过期......";
    }

    private void putToken(String name, String token) {
        token = token.trim();
        if (!token.startsWith("Bearer")) {
            token = "Bearer " + token;
        }
        ScheduleCheckInTask.checkInMap.put(name, token);
    }

}

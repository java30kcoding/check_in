package cn.itlou.check.api;

import cn.itlou.check.core.ScheduleCheckInTask;
import cn.itlou.check.util.TokenUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class CheckInformationApi {

    @GetMapping("/checkTokenTime")
    public String checkTokenTime(@RequestParam("name") String name) {
        if (!ScheduleCheckInTask.checkInMap.containsKey(name) && !ScheduleCheckInTask.emailMap.containsKey(name)) {
            return "不存在该用户!";
        } else {
            return parseTime(name);
        }
    }

    @GetMapping("/updateToken")
    public String sendMail(@RequestParam("name") String name, @RequestParam("token") String token) {
        if (!ScheduleCheckInTask.checkInMap.containsKey(name)) {
            return "不存在该用户!";
        } else {
            putToken(name, token);
            return "更新token成功\n" + parseTime(name);
        }
    }

    @GetMapping("/updateEmail")
    public String updateEmail(@RequestParam("name") String name, @RequestParam("email") String email) {
        if (!ScheduleCheckInTask.emailMap.containsKey(name)) {
            return "不存在该用户!";
        } else {
            ScheduleCheckInTask.emailMap.put(name, email);
            return "更新email成功";
        }
    }

    @GetMapping("/addCheckUser")
    public String addUser(@RequestParam("name") String name, @RequestParam("token") String token, @RequestParam("email") String email) {
        putToken(name, token);
        ScheduleCheckInTask.emailMap.put(name, email);
        return "更新成功";
    }

    @GetMapping("/deleteCheckUser")
    public String deleteCheckUser(@RequestParam("name") String name) {
        if (!ScheduleCheckInTask.checkInMap.containsKey(name) && !ScheduleCheckInTask.emailMap.containsKey(name)) {
            return "不存在该用户!";
        } else {
            ScheduleCheckInTask.checkInMap.remove(name);
            ScheduleCheckInTask.emailMap.remove(name);
            return name + "删除成功";
        }
    }

    private String parseTime(String name) {
        JSONObject userInfo = TokenUtil.parseToken2Json(ScheduleCheckInTask.checkInMap.get(name));
        Long exp = userInfo.getLong("exp");
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

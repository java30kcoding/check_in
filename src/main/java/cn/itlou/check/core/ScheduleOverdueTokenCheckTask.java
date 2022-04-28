package cn.itlou.check.core;

import cn.itlou.check.entity.UserInfo;
import cn.itlou.check.repo.UserInfoRepository;
import cn.itlou.check.util.TokenUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
public class ScheduleOverdueTokenCheckTask {

    @Resource
    JavaMailSender javaMailSender;
    @Resource
    UserInfoRepository userInfoRepository;

    // 每天下午五点校验token是否过期
    @Scheduled(cron = "0 0 17 * * ?")
    public void checkOverdueToken() {
        log.info("start to check overdue token......");
        List<UserInfo> userInfos = userInfoRepository.findAll();
        for (UserInfo userInfo : userInfos) {
            String name = userInfo.getCheckName();
            JSONObject userInfoJson = TokenUtil.parseToken2Json(userInfo.getToken());
            Long exp = userInfoJson.getLong("exp");
            Long current = System.currentTimeMillis() / 1000;
            // token已过期
            if (current - exp >= 0) {
                sendOverdueMail(name, userInfo.getEmailAddress());
            }
        }
    }

    private void sendOverdueMail(String username, String emailAddress) {
        log.info("start to send overdue mail......");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("接龙管家token逾期提醒");
        String from = new String(("接龙管家打卡机器人 <xxxxxxxx@xx.com>").getBytes(StandardCharsets.UTF_8));
        message.setFrom(from);
        message.setTo(emailAddress);
        message.setSentDate(new Date());
        String prefix = "您的token已过期，请及时登录接龙管家获取最新token，通过以下链接扫码更新您的token: \n\n\n\n";
        message.setText(prefix + "http://localhost:19020/index?name=" + username + "&email=" + emailAddress);
        javaMailSender.send(message);
    }

}

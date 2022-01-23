package cn.itlou.check.core;

import cn.itlou.check.http.SimplePostJieLongCheckIn;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 定时打卡任务
 *
 * @author yuanyl
 * @since 2022-01-23 14:00
 */
@Slf4j
@RestController
public class ScheduleCheckInTask {

    private static final Map<String, String> checkInMap = Maps.newHashMap();

    static {
        checkInMap.put("苑雨楼", "Bearer xxxxvRTVoQxxRmtlcUl4aFZrIixxpLmpxxxxCI6MTY0MxxIjoxNjQyOTk4NjI0xxx7ZNYQE1M9xxxYw");
    }

    @Resource
    JavaMailSender javaMailSender;

    @RequestMapping("check")
    @Scheduled(cron = "0 0 8 * * ?")
    public void checkIn() {
        List<String> checkInSuccessName = Lists.newArrayList();
        List<String> checkInFailName = Lists.newArrayList();

        for (Map.Entry<String, String> entry : checkInMap.entrySet()) {
            String response = SimplePostJieLongCheckIn.checkIn(entry.getKey(), entry.getValue());
            if (!Strings.isNullOrEmpty(response)) {
                JSONObject resultJson = JSON.parseObject(response);
                if ("000001".equals(resultJson.getString("Type")) && "打卡成功".equals(resultJson.getString("Data"))) {
                    checkInSuccessName.add(entry.getKey());
                } else if ("000000".equals(resultJson.getString("Type"))) {
                    checkInFailName.add(entry.getKey() + ": " + resultJson.getString("Data") + "\n");
                }
            }
        }
        String nameSuccess = Joiner.on(",").skipNulls().join(checkInSuccessName);
        String nameFail = Joiner.on(",").skipNulls().join(checkInFailName);
        sendMail(nameSuccess, nameFail);
    }

    private void sendMail(String successName, String failNameAndMsg) {
        log.info("start to send mail......");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("接龙管家每日打卡");
        String from = new String(("接龙管家打卡机器人 <xxxxxxx@qq.com>").getBytes(StandardCharsets.UTF_8));
        message.setFrom(from);
        message.setTo("xxxxxx@qq.com");
        message.setCc("xxxxxx@qq.com");
        message.setSentDate(new Date());
        String prefix = "以下成员今日打卡成功: \n      ";
        if (!Strings.isNullOrEmpty(failNameAndMsg)) {
            String errorMsg = "以下成员今日打卡失败: \n      ";
            message.setText(prefix + successName + "\n" + errorMsg + failNameAndMsg);
        } else {
            message.setText(prefix + successName);
        }
        javaMailSender.send(message);
    }


}

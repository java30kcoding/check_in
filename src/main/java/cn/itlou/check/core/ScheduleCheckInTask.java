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
import java.util.*;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

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
    @Scheduled(cron = "0 0 7 * * ?")
    public void checkIn() throws InterruptedException {
        List<String> checkInSuccessName = Lists.newArrayList();
        List<String> checkInFailName = Lists.newArrayList();
        DelayQueue<DelayCheckInUser> delayCheckInUsers = randomCheckIn();
        while (delayCheckInUsers.size() != 0) {
            DelayCheckInUser peek = delayCheckInUsers.poll();
            if (peek != null) {
                String name = peek.getName();
                String response = SimplePostJieLongCheckIn.checkIn(name, checkInMap.get(name));
                if (!Strings.isNullOrEmpty(response)) {
                    JSONObject resultJson = JSON.parseObject(response);
                    if ("000001".equals(resultJson.getString("Type")) && "打卡成功".equals(resultJson.getString("Data"))) {
                        checkInSuccessName.add(name);
                    } else if (!"000001".equals(resultJson.getString("Type"))) {
                        checkInFailName.add(name + ": " + resultJson.getString("Data") + "\n");
                    }
                }
            }
            Thread.sleep(5000L);
        }
        String nameSuccess = Joiner.on(",").skipNulls().join(checkInSuccessName);
        String nameFail = Joiner.on(",").skipNulls().join(checkInFailName);
        sendMail(nameSuccess, nameFail);
    }

    /**
     * 添加时间段内随机时间打卡
     */
    private DelayQueue<DelayCheckInUser> randomCheckIn() {
        DelayQueue<DelayCheckInUser> queue = new DelayQueue<>();
        for (String name : checkInMap.keySet()) {
            Random random = new Random();
            int delaySecond = random.nextInt(60 * 90);
            log.info("{} delay check in time {} seconds", name, delaySecond);
            queue.put(new DelayCheckInUser(name, delaySecond, TimeUnit.SECONDS));
        }
        return queue;
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

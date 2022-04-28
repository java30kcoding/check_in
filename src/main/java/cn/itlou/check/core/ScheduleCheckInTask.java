package cn.itlou.check.core;

import cn.itlou.check.entity.CheckConfig;
import cn.itlou.check.entity.UserInfo;
import cn.itlou.check.http.SimplePostJieLongCheckIn;
import cn.itlou.check.repo.CheckConfigRepository;
import cn.itlou.check.repo.UserInfoRepository;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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

    public static final Map<String, String> checkInMap = Maps.newHashMap();
    public static final Map<String, String> emailMap = Maps.newHashMap();

    @Resource
    UserInfoRepository userInfoRepository;
    @Resource
    CheckConfigRepository checkConfigRepository;

    @Resource
    JavaMailSender javaMailSender;

    @RequestMapping("check")
    @Scheduled(cron = "0 0 7 * * ?")
    public void checkIn() throws InterruptedException {
        List<String> checkInSuccessName = Lists.newArrayList();
        List<String> checkInFailName = Lists.newArrayList();
        List<String> checkInFailMsg = Lists.newArrayList();
        List<CheckConfig> checkConfigs = checkConfigRepository.findAll();
        CheckConfig checkConfig = checkConfigs.get(0);
        List<UserInfo> allCheckUser = userInfoRepository.findAll();
        DelayQueue<DelayCheckInUser> delayCheckInUsers = randomCheckIn(allCheckUser);
        while (delayCheckInUsers.size() != 0) {
            DelayCheckInUser peek = delayCheckInUsers.poll();
            if (peek != null) {
                String name = peek.getName();
                String response = SimplePostJieLongCheckIn.checkIn(checkConfig.getCheckUrl(), name, userInfoRepository.findById(name).get().getToken());
                if (!Strings.isNullOrEmpty(response)) {
                    JSONObject resultJson = JSON.parseObject(response);
                    if ("000001".equals(resultJson.getString("Type")) && "打卡成功".equals(resultJson.getString("Data"))) {
                        checkInSuccessName.add(name);
                    } else if (!"000001".equals(resultJson.getString("Type"))) {
                        String result = resultJson.getString("Data");
                        checkInFailMsg.add(name + ": " + result + "\n");
                        if (!result.contains("已被提交")) {
                            checkInFailName.add(name);
                        }
                    } else {
                        checkInFailMsg.add(name + ": 打卡失败，原因未知\n");
                        checkInFailName.add(name);
                    }
                }
            }
            Thread.sleep(5000L);
        }
        String nameSuccess = Joiner.on(",").skipNulls().join(checkInSuccessName);
        String nameFail = Joiner.on(",").skipNulls().join(checkInFailMsg);
        sendMail(nameSuccess, nameFail);
        // 每五分钟重试一次
        if (checkInFailName.size() > 0) {
            checkInSuccessName.clear();
            int retryCount = checkConfig.getRetryCount();
            while (checkInFailName.size() > 0 && retryCount > 0) {
                Iterator<String> iterator = checkInFailName.iterator();
                while (iterator.hasNext()) {
                    String name = iterator.next();
                    String response = SimplePostJieLongCheckIn.checkIn(checkConfig.getCheckUrl(), name, userInfoRepository.findById(name).get().getToken());
                    if (!Strings.isNullOrEmpty(response)) {
                        JSONObject resultJson = JSON.parseObject(response);
                        if ("000001".equals(resultJson.getString("Type")) && "打卡成功".equals(resultJson.getString("Data"))) {
                            checkInSuccessName.add(name);
                            iterator.remove();
                        } else if (!"000001".equals(resultJson.getString("Type"))) {
                            log.warn("retry check fail, reason: {}", resultJson.getString("Data"));
                        } else {
                            log.warn("retry check fail, reason: {}", "打卡失败，原因未知");
                        }
                    }
                }
                retryCount--;
                if (checkInFailName.size() == 0) {
                    break;
                }
                Thread.sleep(60 * 5 * 1000L);
            }
            nameSuccess = Joiner.on(",").skipNulls().join(checkInSuccessName);
            nameFail = Joiner.on(",").skipNulls().join(checkInFailName);
            sendMail(nameSuccess, nameFail);
        }
    }

    /**
     * 添加时间段内随机时间打卡
     */
    private DelayQueue<DelayCheckInUser> randomCheckIn(List<UserInfo> userInfos) {
        DelayQueue<DelayCheckInUser> queue = new DelayQueue<>();
        for (UserInfo userInfo : userInfos) {
            Random random = new Random();
            int delaySecond = random.nextInt(60 * 60);
            log.info("{} delay check in time {} seconds", userInfo.getCheckName(), delaySecond);
            queue.put(new DelayCheckInUser(userInfo.getCheckName(), delaySecond, TimeUnit.SECONDS));
        }
        return queue;
    }

    private void sendMail(String successName, String failNameAndMsg) {
        log.info("start to send mail......");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("接龙管家每日打卡");
        String from = new String(("接龙管家打卡机器人 <xxxxxxxxx@qq.com>").getBytes(StandardCharsets.UTF_8));
        message.setFrom(from);
        Set<String> toList = Sets.newHashSet();
        List<UserInfo> all = userInfoRepository.findAll();
        for (UserInfo userInfo : all) {
            toList.add(userInfo.getEmailAddress());
        }
        String[] strings = toList.toArray(new String[]{});
        message.setTo(strings);
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

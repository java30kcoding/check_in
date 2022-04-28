package cn.itlou.check.api;

import cn.itlou.check.entity.CheckConfig;
import cn.itlou.check.entity.UserInfo;
import cn.itlou.check.http.CheckQRCode;
import cn.itlou.check.http.GetWXQRCode;
import cn.itlou.check.http.LoginJieLong;
import cn.itlou.check.repo.CheckConfigRepository;
import cn.itlou.check.repo.UserInfoRepository;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
public class CoreApi {

    @Resource
    UserInfoRepository userInfoRepository;
    @Resource
    CheckConfigRepository checkConfigRepository;

    @GetMapping("login")
    public String login(@RequestParam String key, @RequestParam String name, @RequestParam String email) {
        log.info("{} start to login......", name);
        String token = LoginJieLong.login(key);
        if (Strings.isNullOrEmpty(token)) {
            return "0";
        } else {
            Optional<UserInfo> userInfoRepositoryById = userInfoRepository.findById(name);
            UserInfo userInfo;
            if (userInfoRepositoryById.isPresent()) {
                userInfo = userInfoRepositoryById.get();
            } else {
                userInfo = new UserInfo();
                userInfo.setCheckName(name);
                userInfo.setEmailAddress(email);
            }
            userInfo.setToken(token);
            userInfoRepository.save(userInfo);
            return "1";
        }
    }

    @GetMapping("/Portal/GetWXQRCode")
    public void getWXQRCode(@RequestParam String key, HttpServletResponse resp) throws IOException {
        log.info("request key is: {}", key);
        InputStream wxqrCode = GetWXQRCode.getWXQRCode(key);
        if (wxqrCode != null) {
            resp.setContentType(MediaType.IMAGE_PNG_VALUE);
            IOUtils.copy(wxqrCode, resp.getOutputStream());
        }
    }

    @GetMapping("/Portal/CheckLoginStatus")
    public int checkLoginStatus(@RequestParam String key, HttpServletResponse resp) throws IOException {
        return CheckQRCode.checkWXQRCodeStatus(key);
    }

    @GetMapping("/updateEmail")
    public String updateEmail(@RequestParam("name") String name, @RequestParam("email") String email) {
        Optional<UserInfo> userInfoRepositoryById = userInfoRepository.findById(name);
        UserInfo userInfo;
        if (userInfoRepositoryById.isPresent()) {
            userInfo = userInfoRepositoryById.get();
            userInfo.setEmailAddress(email);
            userInfoRepository.save(userInfo);
            return "更新email成功";
        } else {
            return "不存在该用户!";
        }
    }

    @GetMapping("/deleteCheckUser")
    public String deleteCheckUser(@RequestParam("name") String name) {
        Optional<UserInfo> userInfoRepositoryById = userInfoRepository.findById(name);
        if (userInfoRepositoryById.isPresent()) {
            userInfoRepository.deleteById(name);
            return "删除用户成功";
        } else {
            return "不存在该用户!";
        }
    }

    @GetMapping("/config")
    public void config() {
        List<CheckConfig> checkConfigs = checkConfigRepository.findAll();
        for (CheckConfig checkConfig : checkConfigs) {
            log.info(JSON.toJSONString(checkConfig));
        }
    }

    @GetMapping("/user")
    public void user() {
        List<UserInfo> userInfos = userInfoRepository.findAll();
        for (UserInfo userInfo : userInfos) {
            log.info(JSON.toJSONString(userInfo));
        }
    }

}

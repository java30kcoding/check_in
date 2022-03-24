package cn.itlou.check.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TokenUtil {

    public static JSONObject parseToken2Json(String tokenStr) {
        String token = tokenStr.split(" ")[1];
        String userInfoBase64 = token.split("\\.")[1] + "=";
        BASE64Decoder decoder = new BASE64Decoder();
        String userInfoJson = null;
        try {
            userInfoJson = new String(decoder.decodeBuffer(userInfoBase64), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JSON.parseObject(userInfoJson);
    }

}

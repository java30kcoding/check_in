package cn.itlou.check.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

@Slf4j
public class CheckQRCode {

    private static final String QRCODE_URL = "https://jielong.co/Portal/CheckLoginStatus?key=";

    public static int checkWXQRCodeStatus(String key) {
        log.info("request jielongguanjia to check qrcode status key is: {}", key);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(QRCODE_URL + key);
        try {
            get.addHeader("Content-Type", "application/json");
            get.addHeader("Accept", "*/*");
            get.addHeader("Accept-Encoding", "gzip, deflate, br");
            get.addHeader("Connection", "keep-alive");
            HttpResponse response = client.execute(get);
            if (response.getStatusLine().getStatusCode() == 200) {
                String responseStr = EntityUtils.toString(response.getEntity());
                log.info("request jielongguanjia check qrcode done, status is: {}", responseStr);
                return Integer.parseInt(responseStr);
            } else {
                log.error("request jielongguanjia get qrcode fail");
                return 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("request jielongguanjia get qrcode find exception: \n", e);
            return 0;
        }
    }

}

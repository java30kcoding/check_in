package cn.itlou.check.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class GetWXQRCode {

    private static final String QRCODE_URL = "https://jielong.co/Portal/GetWXQRCode?key=";

    public static InputStream getWXQRCode(String key) {
        log.info("request jielongguanjia to get qrcode key is: {}", key);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(QRCODE_URL + key + "&returnUrl=");
        try {
            get.addHeader("Content-Type", "application/json");
            get.addHeader("Accept", "*/*");
            get.addHeader("Accept-Encoding", "gzip, deflate, br");
            get.addHeader("Connection", "keep-alive");
            HttpResponse response = client.execute(get);
            if (response.getStatusLine().getStatusCode() == 200) {
                InputStream content = response.getEntity().getContent();
                log.info("request jielongguanjia done available size is: {}", content.available());
                return content;
            } else {
                log.error("request jielongguanjia get qrcode fail");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("request jielongguanjia get qrcode find exception: \n", e);
            return null;
        }
    }

}

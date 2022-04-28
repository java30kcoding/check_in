package cn.itlou.check.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 请求打卡接口
 *
 * @author yuanyl
 * @since 2022-01-23 13:45
 */
@Slf4j
public class SimplePostJieLongCheckIn {

    private static final int THREAD_ID = 12345678;

    public static String checkIn(String url, String name, String authorization) {
        log.info("request jielongguanjia to checkin name is: {}", name);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        try {
            post.addHeader("Content-Type", "application/json");
            post.addHeader("Accept", "*/*");
            post.addHeader("Accept-Encoding", "gzip, deflate, br");
            post.addHeader("Connection", "keep-alive");
            post.addHeader("Authorization", authorization);
            StringEntity stringEntity =
                    new StringEntity("{\"Id\":\"0\",\"ThreadId\":" + THREAD_ID + ",\"Number\":\"\",\"Signature\":\"" + name + "\",\"RecordValues\":[{\"FieldId\":1,\"Texts\":[\"否\"],\"Values\":[\"2\"],\"Files\":[],\"OtherValue\":\"\",\"HasValue\":true},{\"FieldId\":2,\"Texts\":[\"否\"],\"Values\":[\"2\"],\"Files\":[],\"OtherValue\":\"\",\"HasValue\":true},{\"FieldId\":3,\"Texts\":[],\"Values\":[],\"Files\":[],\"OtherValue\":\"\"}],\"DateTarget\":\"\",\"IsNeedManualAudit\":false,\"MinuteTarget\":-1,\"IsNameNumberComfirm\":false}", StandardCharsets.UTF_8);
            post.setEntity(stringEntity);
            HttpResponse response = client.execute(post);
            String responseStr = EntityUtils.toString(response.getEntity());
            log.info("request jielongguanjia result: {}", responseStr);
            return responseStr;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("request jielongguanjia find exception: \n", e);
            return "";
        }
    }

}

package cn.itlou.check.http;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;

import java.io.IOException;

@Slf4j
public class LoginJieLong {

    private static final String JIE_LONG_LOGIN_URL = "http://www.jielong.co/Portal/Login?key=";

    public static String login(String key) {
        final String[] token = {""};
        BasicCookieStore cookieStore = new BasicCookieStore();
        CookieSpecProvider easySpecProvider = context -> new BrowserCompatSpec() {
            @Override
            public void validate(Cookie cookie, CookieOrigin origin) {
                log.info("Cookie accepted [" + cookie.getValue() + "]");
                String tokenStr = cookie.getValue();
                if (!Strings.isNullOrEmpty(tokenStr) && tokenStr.startsWith("Bearer")) {
                    tokenStr = tokenStr.replace("%20", " ");
                    token[0] = tokenStr;
                }
            }
        };
        Registry<CookieSpecProvider> r = RegistryBuilder
                .<CookieSpecProvider> create()
                .register(CookieSpecs.BEST_MATCH, new BestMatchSpecFactory())
                .register(CookieSpecs.BROWSER_COMPATIBILITY,
                        new BrowserCompatSpecFactory())
                .register("easy", easySpecProvider).build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec("easy").setSocketTimeout(10000)
                .setConnectTimeout(10000).build();

        CloseableHttpClient client = HttpClients.custom()
                .setDefaultCookieSpecRegistry(r)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore)
                .build();
        log.info("request jielongguanjia to checkin key is: {}", key);
        HttpGet get = new HttpGet(JIE_LONG_LOGIN_URL + key);
        try {
            get.addHeader("Content-Type", "application/json");
            get.addHeader("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"100\", \"Google Chrome\";v=\"100\"");
            get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36");
            get.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            get.addHeader("Accept-Encoding", "gzip, deflate, br");
            get.addHeader("Host", "www.jielong.co");
            get.addHeader("Referer", "https://www.jielong.co/");
            get.addHeader("Connection", "keep-alive");
            get.addHeader("Accept-Language", "zh-CN,zh;q=0.9");
            get.addHeader("sec-ch-ua-platform", "\"Windows\"");
            get.addHeader("sec-ch-ua-mobile", "?0");
            get.addHeader("Sec-Fetch-Dest", "document");
            get.addHeader("Sec-Fetch-Mode", "navigate");
            get.addHeader("Sec-Fetch-Site", "same-origin");
            get.addHeader("Upgrade-Insecure-Requests", "1");
            client.execute(get);
            log.info("login success and token is: {}", token[0]);
            return token[0];
        } catch (IOException e) {
            e.printStackTrace();
            log.error("request jielongguanjia find exception: \n", e);
            return "";
        }
    }

}

package cn.itlou.check.core;

import lombok.Data;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 延迟对象
 *
 * @author yuanyl
 * @since 2022-01-24 14:00
 */
@Data
public class DelayCheckInUser implements Delayed {

    private String name;
    private long time;

    public DelayCheckInUser(String name, long time, TimeUnit unit) {
        this.name = name;
        this.time = System.currentTimeMillis() + (time > 0 ? unit.toMillis(time) : 0);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return time - System.currentTimeMillis();
    }

    @Override
    public int compareTo(Delayed o) {
        DelayCheckInUser user = (DelayCheckInUser) o;
        long diff = this.time - user.time;
        if (diff <= 0) {
            return -1;
        } else {
            return 1;
        }
    }

}

package cn.itlou.check;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CheckApplication {

	public static void main(String[] args) {
		SpringApplication.run(CheckApplication.class, args);
	}

}

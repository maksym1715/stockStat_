package team606.stockStat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
public class StockStatApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockStatApplication.class, args);
	}

}



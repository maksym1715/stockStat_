package team606.stockStat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import team606.stockStat.security.SecurityConfig;

@SpringBootApplication
@EnableTransactionManagement
@Import({ SecurityConfig.class})
public class StockStatApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockStatApplication.class, args);
	}

}



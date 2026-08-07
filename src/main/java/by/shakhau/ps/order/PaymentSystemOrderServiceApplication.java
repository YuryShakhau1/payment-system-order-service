package by.shakhau.ps.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients
@EnableRetry
public class PaymentSystemOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentSystemOrderServiceApplication.class, args);
    }
}

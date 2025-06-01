package gc.garcol.caferaft.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class CafeRaftApplication {

    public static void main(String[] args) {
        SpringApplication.run(CafeRaftApplication.class, args);
    }
}

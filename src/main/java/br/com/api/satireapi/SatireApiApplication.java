package br.com.api.satireapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.modulith.Modulithic;

@Modulithic(systemName = "Satire API")
@SpringBootApplication
@EnableScheduling
public class SatireApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SatireApiApplication.class, args);
    }
}

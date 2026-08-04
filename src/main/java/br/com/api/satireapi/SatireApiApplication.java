package br.com.api.satireapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

@Modulithic(systemName = "Satire API")
@SpringBootApplication
public class SatireApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SatireApiApplication.class, args);
    }
}

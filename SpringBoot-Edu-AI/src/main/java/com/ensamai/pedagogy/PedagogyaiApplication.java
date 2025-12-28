package com.ensamai.pedagogy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
// FORCE Spring to look in these packages:
@ComponentScan(basePackages = "com.ensamai.pedagogy")
@EnableJpaRepositories(basePackages = "com.ensamai.pedagogy.repository")
@EntityScan(basePackages = "com.ensamai.pedagogy.model")
public class PedagogyaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PedagogyaiApplication.class, args);
        System.out.println("=========================================");
        System.out.println("    🚀 APP STARTED - CHECKING DATA...    ");
        System.out.println("=========================================");
    }
}
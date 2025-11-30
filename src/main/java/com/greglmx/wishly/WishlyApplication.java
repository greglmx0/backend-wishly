package com.greglmx.wishly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WishlyApplication {
    public static void main(String[] args) {
        SpringApplication.run(WishlyApplication.class, args);

        System.out.println("Application Wishly démarrée avec succès !");
    }
}

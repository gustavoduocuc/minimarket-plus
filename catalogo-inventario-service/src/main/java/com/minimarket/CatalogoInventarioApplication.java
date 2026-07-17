package com.minimarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class CatalogoInventarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogoInventarioApplication.class, args);
    }
}

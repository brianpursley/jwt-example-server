package com.cinlogic.jwtexampleserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ConfigurationPropertiesScan
@EnableJpaRepositories
@EntityScan
@SpringBootApplication
public class JwtExampleServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JwtExampleServerApplication.class, args);
    }

}

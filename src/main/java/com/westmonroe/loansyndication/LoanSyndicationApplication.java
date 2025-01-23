package com.westmonroe.loansyndication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@OpenAPIDefinition(
    info = @Info(
        title = "Lamina API",
        version = "1.0",
        description = "This API exposes endpoints for the Lamina application.",
        contact = @Contact(url = "https://westmonroe.com", name = "Joseph Clevenger", email = "jclevenger@westmonroe.com")
    )
)
@SpringBootApplication
@EnableScheduling
public class LoanSyndicationApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanSyndicationApplication.class, args);
    }

}

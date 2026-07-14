package moh.gov.zm.lis;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@OpenAPIDefinition(
        servers = @Server(url = "http://localhost:8089", description = "Local server"),
        info = @Info(title = "LIS", version = "1.0", description = "Laboratory Information Service API"),
        security = { @SecurityRequirement(name = "bearerAuth") }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SpringBootApplication
public class LisApplication {

    static void main(String[] args) {
        SpringApplication.run(LisApplication.class, args);
    }

}

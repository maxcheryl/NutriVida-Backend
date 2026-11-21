package cl.duocuc.nutrivida_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentación Swagger/OpenAPI del API. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nutrividaOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Clínica NutriVida API")
                .description("""
                        API REST del ecommerce de la Clínica NutriVida.
                        Autenticación mediante JWT almacenado en cookie httpOnly:
                        el access token vence a los 30 minutos y el refresh token a los 7 días.""")
                .version("1.0")
                .license(new License().name("DuocUC - DSY1104")));
    }
}

package cl.duocuc.nutrivida_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Define la información general que aparece en Swagger. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nutrividaOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Clínica NutriVida API")
                .description("""
                        API REST del ecommerce de la Clínica NutriVida.
                        Autenticación mediante cookies JWT HttpOnly: `nutrivida_token` (access, 30 min)
                        y `nutrivida_refresh` (refresh, 7 días). Los errores de la API se responden
                        en JSON con `mensaje` y, cuando aplica, `campos`.""")
                .version("1.0")
                .license(new License().name("DuocUC - DSY1104")));
    }
}

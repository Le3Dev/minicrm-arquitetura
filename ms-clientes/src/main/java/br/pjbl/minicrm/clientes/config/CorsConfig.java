package br.pjbl.minicrm.clientes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Só o BFF e a Function chamam este serviço; CORS fica aberto apenas se CORS_ORIGINS for definido. */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] origens;

    public CorsConfig(@Value("${app.cors-origins:}") String origens) {
        this.origens = origens.isBlank() ? new String[0] : origens.split("\\s*,\\s*");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (origens.length > 0) {
            registry.addMapping("/**").allowedOrigins(origens).allowedMethods("GET", "POST", "PATCH", "DELETE");
        }
    }
}

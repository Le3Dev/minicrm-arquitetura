package br.pjbl.minicrm.clientes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI documentacao(@Value("${app.url-publica:}") String urlPublica) {
        OpenAPI api = new OpenAPI().info(new Info()
                .title("Mini CRM — MS Clientes")
                .version("1.0.0")
                .description("Clientes e interações do Mini CRM, persistidos no MongoDB Atlas. "
                        + "O status do cliente passa a Ativo quando a Azure Function consome o evento OportunidadeGanha."));
        if (!urlPublica.isBlank()) {
            api.servers(List.of(new Server().url(urlPublica)));
        }
        return api;
    }
}

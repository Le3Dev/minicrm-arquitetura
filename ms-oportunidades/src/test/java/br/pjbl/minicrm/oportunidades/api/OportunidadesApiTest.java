package br.pjbl.minicrm.oportunidades.api;

import br.pjbl.minicrm.oportunidades.domain.EventoPublisher;
import br.pjbl.minicrm.oportunidades.infrastructure.eventos.LogEventoPublisher;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Aplicação inteira com H2 (modo SQL Server) e a migração Flyway real. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OportunidadesApiTest {

    @Autowired
    private MockMvc http;
    @Autowired
    private EventoPublisher eventos;

    @Test
    void criaFechaComoGanhoEPublicaOEvento() throws Exception {
        String corpo = http.perform(post("/oportunidades").contentType(MediaType.APPLICATION_JSON).content("""
                        {"clienteId":"c1","titulo":"Plano anual","valor":18500,"dataPrevista":"2026-12-15","responsavelId":"u2"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.etapa").value("Prospecção"))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(corpo, "$.id");

        http.perform(patch("/oportunidades/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"etapa\":\"Fechado\",\"resultado\":\"ganho\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("ganho"));

        assertThat(((LogEventoPublisher) eventos).publicados()).anySatisfy(e -> assertThat(e.oportunidadeId()).isEqualTo(id.longValue()));

        http.perform(patch("/oportunidades/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"etapa\":\"Contato\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("MOVIMENTACAO_INVALIDA"));
    }

    @Test
    void validaEntradaEDocumentaNoSwagger() throws Exception {
        http.perform(post("/oportunidades").contentType(MediaType.APPLICATION_JSON).content("{\"titulo\":\"A\",\"valor\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"));
        http.perform(patch("/oportunidades/999").contentType(MediaType.APPLICATION_JSON).content("{\"etapa\":\"Contato\"}"))
                .andExpect(status().isNotFound());
        http.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(jsonPath("$.paths['/oportunidades/{id}']").exists());
    }
}

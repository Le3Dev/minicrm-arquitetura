package br.pjbl.minicrm.clientes.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Sobe a aplicação inteira (perfil memoria, sem banco) e exercita a API por HTTP. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("memoria")
class ClientesApiTest {

    @Autowired
    private MockMvc http;

    private String cadastrar(String nome, String responsavel) throws Exception {
        MvcResult r = http.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome":"%s","empresa":"Acme Ltda","email":"%s@acme.com","telefone":"(41) 99999-0000","responsavelId":"%s"}
                        """.formatted(nome, nome.toLowerCase().replace(' ', '.'), responsavel)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("Prospect"))
                .andReturn();
        return JsonPath.read(r.getResponse().getContentAsString(), "$.id");
    }

    @Test
    void cicloCompletoDoCliente() throws Exception {
        String id = cadastrar("Mariana Souza", "u2");

        http.perform(patch("/clientes/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"responsavelId\":\"u5\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.responsavelId").value("u5"));

        http.perform(post("/clientes/" + id + "/interacoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"Reunião\",\"obs\":\"Apresentei a proposta\",\"autorId\":\"u5\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.tipo").value("Reunião"));

        http.perform(get("/clientes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente.totalInteracoes").value(1))
                .andExpect(jsonPath("$.interacoes[0].obs").value("Apresentei a proposta"));

        http.perform(delete("/clientes/" + id)).andExpect(status().isNoContent());
        http.perform(get("/clientes/" + id)).andExpect(jsonPath("$.cliente.status").value("Inativo"));
        http.perform(get("/clientes").param("q", "mariana")).andExpect(jsonPath("$.paginacao.total").value(0));
        http.perform(patch("/clientes/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Outro\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CLIENTE_EXCLUIDO"));
    }

    @Test
    void pesquisaComFiltrosEPaginacaoNoFormatoDoMiniCrm() throws Exception {
        cadastrar("Carlos Dias", "u2");
        cadastrar("Beatriz Nunes", "u7");

        http.perform(get("/clientes").param("responsavelId", "u7,u8").param("porPagina", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].nome").value("Beatriz Nunes"))
                .andExpect(jsonPath("$.paginacao.porPagina").value(1))
                .andExpect(jsonPath("$.filtros.responsaveis[0]").value("u7"));
    }

    @Test
    void ativacaoPorEventoEhIdempotente() throws Exception {
        String id = cadastrar("Daniel Rocha", "u2");
        String corpo = "{\"eventoId\":\"ev-77\",\"oportunidadeId\":\"77\"}";

        http.perform(post("/clientes/" + id + "/ativacao").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk()).andExpect(jsonPath("$.resultado").value("ATIVADO"))
                .andExpect(jsonPath("$.cliente.status").value("Ativo"));
        http.perform(post("/clientes/" + id + "/ativacao").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(jsonPath("$.resultado").value("REPETIDO"));
    }

    @Test
    void errosDeValidacaoVemComTodosOsCampos() throws Exception {
        http.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"A\",\"email\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"))
                .andExpect(jsonPath("$.erros", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$.erros[*]", hasItem(startsWith("responsavelId:"))));
        http.perform(get("/clientes/nao-existe")).andExpect(status().isNotFound()).andExpect(jsonPath("$.codigo").value("NAO_ENCONTRADO"));
        http.perform(get("/clientes").param("status", "Lead")).andExpect(status().isBadRequest());
    }

    @Test
    void swaggerPublicaTodasAsRotas() throws Exception {
        http.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Mini CRM — MS Clientes"))
                .andExpect(jsonPath("$.paths['/clientes']").exists())
                .andExpect(jsonPath("$.paths['/clientes/{id}/interacoes']").exists())
                .andExpect(jsonPath("$.paths['/clientes/{id}/ativacao']").exists());
        http.perform(get("/actuator/health")).andExpect(status().isOk());
    }
}

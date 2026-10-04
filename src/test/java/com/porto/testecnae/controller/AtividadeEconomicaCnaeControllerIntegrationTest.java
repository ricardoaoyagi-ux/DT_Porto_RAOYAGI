package com.porto.testecnae.controller;


import com.porto.testecnae.domain.AtividadeEconomicaCnae;
import com.porto.testecnae.repository.AtividadeEconomicaCnaeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AtividadeEconomicaCnaeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AtividadeEconomicaCnaeRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void deveRetornar404QuandoCnaeNaoExistir() throws Exception {

        mockMvc.perform(
                        get("/api/cnaes/codigo")
                                .param("codigo", "9999")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("CNAE não encontrado: 9999"));
    }

    @Test
    void deveRetornarCnaeQuandoCodigoExistir() throws Exception {

        var cnae = AtividadeEconomicaCnae.builder()
                .codigo("6201-5/01")
                .descricao("Desenvolvimento de programas de computador")
                .secao("Informação e comunicação")
                .build();

        repository.save(cnae);

        mockMvc.perform(
                        get("/api/cnaes/codigo")
                                .param("codigo", "6201-5/01")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("6201-5/01"))
                .andExpect(jsonPath("$.descricao")
                        .value("Desenvolvimento de programas de computador"))
                .andExpect(jsonPath("$.secao")
                        .value("Informação e comunicação"));
    }

    @Test
    void deveBuscarCnaePorDescricaoIgnorandoMaiusculasEMinusculas() throws Exception {

        var cnae = AtividadeEconomicaCnae.builder()
                .codigo("6201-5/01")
                .descricao("Desenvolvimento de Software")
                .secao("Informação e comunicação")
                .build();

        repository.save(cnae);

        mockMvc.perform(
                        get("/api/cnaes/buscar")
                                .param("termo", "SOFTWARE")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].codigo").value("6201-5/01"))
                .andExpect(jsonPath("$[0].descricao")
                        .value("Desenvolvimento de Software"));
    }

    @Test
    void deveBuscarPorDescricaoParcialEOrdenarPorCodigo() throws Exception {

        var cnae1 = AtividadeEconomicaCnae.builder()
                .codigo("6202-3/00")
                .descricao("Consultoria em tecnologia da informação")
                .secao("Informação e comunicação")
                .build();

        var cnae2 = AtividadeEconomicaCnae.builder()
                .codigo("6201-5/01")
                .descricao("Desenvolvimento em tecnologia da informação")
                .secao("Informação e comunicação")
                .build();

        repository.saveAll(List.of(cnae1, cnae2));

        mockMvc.perform(
                        get("/api/cnaes/buscar")
                                .param("termo", "tecnologia")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].codigo").value("6201-5/01"))
                .andExpect(jsonPath("$[1].codigo").value("6202-3/00"));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremCnaes() throws Exception {

        mockMvc.perform(get("/api/cnaes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deveListarTodosOsCnaes() throws Exception {

        var cnae1 = AtividadeEconomicaCnae.builder()
                .codigo("6201-5/01")
                .descricao("Desenvolvimento de programas")
                .secao("Informação e comunicação")
                .build();

        var cnae2 = AtividadeEconomicaCnae.builder()
                .codigo("6202-3/00")
                .descricao("Consultoria em tecnologia")
                .secao("Informação e comunicação")
                .build();

        repository.saveAll(List.of(cnae1, cnae2));

        mockMvc.perform(get("/api/cnaes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
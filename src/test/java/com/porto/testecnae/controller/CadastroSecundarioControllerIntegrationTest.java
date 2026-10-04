package com.porto.testecnae.controller;

import com.porto.testecnae.domain.AtividadeEconomicaCnae;
import com.porto.testecnae.repository.AtividadeEconomicaCnaeRepository;
import com.porto.testecnae.repository.CadastroSecundarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.porto.testecnae.domain.CadastroSecundario;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CadastroSecundarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AtividadeEconomicaCnaeRepository cnaeRepository;

    @Autowired
    private CadastroSecundarioRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAllInBatch();
        cnaeRepository.deleteAllInBatch();
    }

    @Test
    void deveCadastrarQuandoDadosForemValidos() throws Exception {

        var cnae = AtividadeEconomicaCnae.builder()
                .codigo("6201-5/01")
                .descricao("Desenvolvimento de programas")
                .secao("Informação e comunicação")
                .build();

        cnaeRepository.save(cnae);

        var json = """
            {
              "nomeFantasia": "Tech Porto",
              "documento": "12345678000199",
              "codigoCnae": "6201-5/01"
            }
            """;

        mockMvc.perform(
                        post("/api/cadastros-secundarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeFantasia").value("Tech Porto"))
                .andExpect(jsonPath("$.documento").value("12345678000199"));
        assertEquals(1, repository.count());
    }
    @Test
    void deveRetornar400QuandoDocumentoNaoForInformado() throws Exception {

        var json = """
            {
              "nomeFantasia": "Tech Porto",
              "codigoCnae": "6201-5/01"
            }
            """;

        mockMvc.perform(
                        post("/api/cadastros-secundarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Documento é obrigatório"));

        assertEquals(0, repository.count());
    }
    @Test
    void deveRetornar400ComTodasAsMensagensQuandoVariosCamposForemInvalidos() throws Exception {

        mockMvc.perform(
                        post("/api/cadastros-secundarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(
                        "Código CNAE é obrigatório; Documento é obrigatório; Nome fantasia é obrigatório"));

        assertEquals(0, repository.count());
    }

    @Test
    void deveRetornar404QuandoCadastrarComCnaeInexistente() throws Exception {

        var json = """
            {
              "nomeFantasia": "Tech Porto",
              "documento": "12345678000199",
              "codigoCnae": "9999"
            }
            """;

        mockMvc.perform(
                        post("/api/cadastros-secundarios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("CNAE não encontrado: 9999"));

        assertEquals(0, repository.count());
    }

    @Test
    void deveValidarCnaeExistente() throws Exception {

        var cnae = AtividadeEconomicaCnae.builder()
                .codigo("6201-5/01")
                .descricao("Desenvolvimento de programas")
                .secao("Informação e comunicação")
                .build();

        cnaeRepository.save(cnae);

        mockMvc.perform(
                        get("/api/cadastros-secundarios/validar-cnae")
                                .param("codigoCnae", "6201-5/01")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("6201-5/01"))
                .andExpect(jsonPath("$.descricao").value("Desenvolvimento de programas"));
    }

    @Test
    void deveRetornar404AoValidarCnaeInexistente() throws Exception {

        mockMvc.perform(
                        get("/api/cadastros-secundarios/validar-cnae")
                                .param("codigoCnae", "9999")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("CNAE não encontrado: 9999"));
    }

    @Test
    void deveListarCadastrosSecundarios() throws Exception {

        var cnae = AtividadeEconomicaCnae.builder()
                .codigo("6201-5/01")
                .descricao("Desenvolvimento de programas")
                .secao("Informação e comunicação")
                .build();

        cnaeRepository.save(cnae);

        var cadastro = CadastroSecundario.builder()
                .nomeFantasia("Tech Porto")
                .documento("12345678000199")
                .cnae(cnae)
                .build();

        repository.save(cadastro);

        mockMvc.perform(get("/api/cadastros-secundarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nomeFantasia").value("Tech Porto"))
                .andExpect(jsonPath("$[0].documento").value("12345678000199"));
    }
}
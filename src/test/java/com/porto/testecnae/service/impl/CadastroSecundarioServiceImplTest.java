package com.porto.testecnae.service.impl;

import com.porto.testecnae.domain.AtividadeEconomicaCnae;
import com.porto.testecnae.domain.CadastroSecundario;
import com.porto.testecnae.dto.CadastroSecundarioRequest;
import com.porto.testecnae.exception.CnaeNaoEncontradoException;
import com.porto.testecnae.repository.AtividadeEconomicaCnaeRepository;
import com.porto.testecnae.repository.CadastroSecundarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class CadastroSecundarioServiceImplTest {

    @Mock
    private CadastroSecundarioRepository repository;

    @Mock
    private AtividadeEconomicaCnaeRepository cnaeRepository;

    @InjectMocks
    private CadastroSecundarioServiceImpl service;


    @Test
    void naoCadastrarCnaeNaoExiste() {

        var request = new CadastroSecundarioRequest(
                "Tech Porto",
                "12345678000199",
                "9999"
        );

        when(cnaeRepository.findByCodigo("9999"))
                .thenReturn(Optional.empty());

        assertThrows(
                CnaeNaoEncontradoException.class,
                () -> service.cadastrar(request)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void deveCadastrarCnaeExiste() {

        var cnae = AtividadeEconomicaCnae.builder()
                .id(1L)
                .codigo("6201-5/01")
                .descricao("Desenvolvimento de programas")
                .secao("Informação e comunicação")
                .build();

        var request = new CadastroSecundarioRequest(
                "Tech Porto",
                "12345678000199",
                "6201-5/01"
        );

        when(cnaeRepository.findByCodigo("6201-5/01"))
                .thenReturn(Optional.of(cnae));

        when(repository.save(any(CadastroSecundario.class)))
                .thenAnswer(invocation -> {
                    CadastroSecundario cadastro = invocation.getArgument(0);
                    cadastro.setId(10L);
                    return cadastro;
                });

        var resultado = service.cadastrar(request);

        assertNotNull(resultado);
        assertEquals(10L, resultado.id());

        var captor = ArgumentCaptor.forClass(CadastroSecundario.class);

        verify(repository).save(captor.capture());

        var cadastroSalvo = captor.getValue();

        assertEquals("Tech Porto", cadastroSalvo.getNomeFantasia());
        assertEquals("12345678000199", cadastroSalvo.getDocumento());
        assertEquals("6201-5/01", cadastroSalvo.getCnae().getCodigo());
    }

    @Test
    void deveValidarCnaeExistente() {

        var cnae = AtividadeEconomicaCnae.builder()
                .id(1L)
                .codigo("6201-5/01")
                .descricao("Desenvolvimento de programas")
                .secao("Informação e comunicação")
                .build();

        when(cnaeRepository.findByCodigo("6201-5/01"))
                .thenReturn(Optional.of(cnae));

        var resultado = service.validarCnae("6201-5/01");

        verify(cnaeRepository).findByCodigo("6201-5/01");

        assertNotNull(resultado);
        assertEquals("6201-5/01", resultado.codigo());
    }

    @Test
    void deveLancarExcecaoAoValidarCnaeInexistente() {

        when(cnaeRepository.findByCodigo("9999"))
                .thenReturn(Optional.empty());

        assertThrows(
                CnaeNaoEncontradoException.class,
                () -> service.validarCnae("9999")
        );
    }

    @Test
    void deveListarTodosOsCadastros() {

        var cnae = AtividadeEconomicaCnae.builder()
                .id(1L)
                .codigo("6201-5/01")
                .descricao("Desenvolvimento de programas")
                .secao("Informação e comunicação")
                .build();

        var cadastro1 = CadastroSecundario.builder()
                .id(1L)
                .nomeFantasia("Empresa A")
                .documento("12345678000199")
                .cnae(cnae)
                .build();

        var cadastro2 = CadastroSecundario.builder()
                .id(2L)
                .nomeFantasia("Empresa B")
                .documento("98765432000199")
                .cnae(cnae)
                .build();

        when(repository.findAll())
                .thenReturn(List.of(cadastro1, cadastro2));

        var resultado = service.listarTodos();

        verify(repository).findAll();

        assertEquals(2, resultado.size());
        assertEquals("Empresa A", resultado.get(0).nomeFantasia());
        assertEquals("Empresa B", resultado.get(1).nomeFantasia());
    }
}
package com.porto.testecnae.service.impl;

import com.porto.testecnae.domain.AtividadeEconomicaCnae;
import com.porto.testecnae.exception.CnaeNaoEncontradoException;
import com.porto.testecnae.repository.AtividadeEconomicaCnaeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtividadeEconomicaCnaeServiceImplTest {

    @Mock
    private AtividadeEconomicaCnaeRepository repository;

    @InjectMocks
    private AtividadeEconomicaCnaeServiceImpl service;

    @Test
    void ExcecaoCnaeNaoExiste() {

        when(repository.findByCodigo("9999"))
                .thenReturn(Optional.empty());

        assertThrows(
                CnaeNaoEncontradoException.class,
                () -> service.buscarPorCodigo("9999")
        );
    }

    @Test
    void buscarPorCodigo() {
        var cnae = AtividadeEconomicaCnae.builder()
                .id(1L)
                .codigo("6201-5/01")
                .descricao("Desenvolvimento de programas de computador sob encomenda")
                .secao("Informação e comunicação")
                .build();

        when(repository.findByCodigo("6201-5/01"))
                .thenReturn(Optional.of(cnae));

        var resultado = service.buscarPorCodigo("6201-5/01");

        assertNotNull(resultado);
        assertEquals("6201-5/01", resultado.codigo());
    }

    @Test
    void deveListarTodosOsCnaes() {

        var cnae1 = AtividadeEconomicaCnae.builder()
                .id(1L)
                .codigo("6201-5/01")
                .descricao("Desenvolvimento de programas")
                .secao("Informação e comunicação")
                .build();

        var cnae2 = AtividadeEconomicaCnae.builder()
                .id(2L)
                .codigo("6202-3/00")
                .descricao("Consultoria em tecnologia")
                .secao("Informação e comunicação")
                .build();

        when(repository.findAll())
                .thenReturn(List.of(cnae1, cnae2));

        var resultado = service.listarTodas();

        assertEquals(2, resultado.size());
        assertEquals("6201-5/01", resultado.get(0).codigo());
        assertEquals("6202-3/00", resultado.get(1).codigo());
    }

    @Test
    void deveBuscarCnaePorDescricao() {

        var cnae = AtividadeEconomicaCnae.builder()
                .id(1L)
                .codigo("6201-5/01")
                .descricao("Desenvolvimento de programas")
                .secao("Informação e comunicação")
                .build();

        when(repository.buscarPorDescricao("Desenvolvimento"))
                .thenReturn(List.of(cnae));

        var resultado = service.buscarPorDescricao("Desenvolvimento");

        verify(repository).buscarPorDescricao("Desenvolvimento");
        assertEquals(1, resultado.size());
        assertEquals("6201-5/01", resultado.getFirst().codigo());
    }
}
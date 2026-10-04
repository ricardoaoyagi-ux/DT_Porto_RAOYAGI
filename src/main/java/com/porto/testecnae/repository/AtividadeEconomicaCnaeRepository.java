package com.porto.testecnae.repository;

import com.porto.testecnae.domain.AtividadeEconomicaCnae;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AtividadeEconomicaCnaeRepository extends JpaRepository<AtividadeEconomicaCnae, Long> {

    Optional<AtividadeEconomicaCnae> findByCodigo(String codigo);

    @Query("""
            select c
            from AtividadeEconomicaCnae c
            where lower(c.descricao) like lower(concat('%',:termo, '%'))
            order by c.codigo
            """)
    List<AtividadeEconomicaCnae> buscarPorDescricao(@Param("termo") String termo);
}

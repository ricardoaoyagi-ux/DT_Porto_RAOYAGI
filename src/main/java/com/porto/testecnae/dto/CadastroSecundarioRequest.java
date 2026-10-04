package com.porto.testecnae.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroSecundarioRequest(

        @NotBlank(message = "Nome fantasia é obrigatório")
        @Size(max = 120, message = "Nome fantasia deve possuir no máximo 120 caracteres")
        String nomeFantasia,

        @NotBlank(message = "Documento é obrigatório")
        @Size(max = 14, message = "Documento deve possuir no máximo 14 caracteres")
        String documento,

        @NotBlank(message = "Código CNAE é obrigatório")
        String codigoCnae
) {
}

package com.porto.testecnae.exception;

public class CnaeNaoEncontradoException extends RuntimeException {

    public CnaeNaoEncontradoException(String codigo) {
        super("CNAE não encontrado: " + codigo);
    }
}
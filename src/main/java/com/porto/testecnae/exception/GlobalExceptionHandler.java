package com.porto.testecnae.exception;

import com.porto.testecnae.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Comparator;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CnaeNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleCnaeNaoEncontrado(
            CnaeNaoEncontradoException ex) {

        var erro = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        // Ordena por campo para que a mensagem seja deterministica quando houver varios erros
        String mensagem = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .sorted(Comparator.comparing(FieldError::getField)
                        .thenComparing(FieldError::getDefaultMessage, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        if (mensagem.isBlank()) {
            mensagem = "Requisição inválida";
        }

        var erro = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                mensagem
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(erro);
    }
}
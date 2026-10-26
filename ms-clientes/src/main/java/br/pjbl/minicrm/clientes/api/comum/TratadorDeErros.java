package br.pjbl.minicrm.clientes.api.comum;

import br.pjbl.minicrm.clientes.domain.DomainException;
import br.pjbl.minicrm.clientes.domain.ValidacaoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;

/** Traduz erros de domínio e de validação para respostas HTTP. */
@RestControllerAdvice
public class TratadorDeErros {

    private static final Map<String, HttpStatus> STATUS = Map.of(
            "VALIDACAO", HttpStatus.BAD_REQUEST,
            "NAO_ENCONTRADO", HttpStatus.NOT_FOUND,
            "CLIENTE_EXCLUIDO", HttpStatus.CONFLICT,
            "CONCORRENCIA", HttpStatus.CONFLICT);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErroResponse> dominio(DomainException e) {
        HttpStatus status = STATUS.getOrDefault(e.codigo(), HttpStatus.UNPROCESSABLE_ENTITY);
        List<String> erros = e instanceof ValidacaoException v ? v.erros() : List.of(e.getMessage());
        return ResponseEntity.status(status).body(new ErroResponse(status.value(), e.codigo(), e.getMessage(), erros));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> beanValidation(MethodArgumentNotValidException e) {
        List<String> erros = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .sorted()
                .toList();
        return ResponseEntity.badRequest().body(new ErroResponse(400, "VALIDACAO", "Dados inválidos", erros));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErroResponse> corpoInvalido(Exception e) {
        return ResponseEntity.badRequest().body(new ErroResponse(400, "VALIDACAO", "Requisição malformada", List.of("JSON ou parâmetro inválido")));
    }
}

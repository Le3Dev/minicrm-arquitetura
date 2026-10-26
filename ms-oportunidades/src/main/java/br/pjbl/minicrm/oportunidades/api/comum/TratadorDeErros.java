package br.pjbl.minicrm.oportunidades.api.comum;

import br.pjbl.minicrm.oportunidades.domain.DomainException;
import br.pjbl.minicrm.oportunidades.domain.ValidacaoException;
import com.azure.core.exception.AzureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

/** Mesmo formato de erro do MS Clientes: { status, codigo, erro, erros[] }. */
@RestControllerAdvice
public class TratadorDeErros {

    public record ErroResponse(int status, String codigo, String erro, List<String> erros) {
    }

    private static final Map<String, HttpStatus> STATUS = Map.of(
            "VALIDACAO", HttpStatus.BAD_REQUEST,
            "MOVIMENTACAO_INVALIDA", HttpStatus.BAD_REQUEST,
            "NAO_ENCONTRADO", HttpStatus.NOT_FOUND,
            "CONCORRENCIA", HttpStatus.CONFLICT);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErroResponse> dominio(DomainException e) {
        HttpStatus s = STATUS.getOrDefault(e.codigo(), HttpStatus.UNPROCESSABLE_ENTITY);
        List<String> erros = e instanceof ValidacaoException v ? v.erros() : List.of(e.getMessage());
        return ResponseEntity.status(s).body(new ErroResponse(s.value(), e.codigo(), e.getMessage(), erros));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException e) {
        List<String> erros = e.getBindingResult().getFieldErrors().stream().map(f -> f.getField() + ": " + f.getDefaultMessage()).sorted().toList();
        return ResponseEntity.badRequest().body(new ErroResponse(400, "VALIDACAO", "Dados inválidos", erros));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpo(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(new ErroResponse(400, "VALIDACAO", "Requisição malformada", List.of("JSON inválido")));
    }

    @ExceptionHandler(AzureException.class)
    public ResponseEntity<ErroResponse> serviceBus(AzureException e) {
        return ResponseEntity.status(503).body(new ErroResponse(503, "EVENTO_NAO_PUBLICADO", "Falha ao publicar o evento no Service Bus", List.of(e.getMessage())));
    }
}

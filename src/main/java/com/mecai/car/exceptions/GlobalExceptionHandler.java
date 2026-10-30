package com.mecai.car.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.mecai.car.dto.ResponseDto;

/**
 * Convierte las excepciones de negocio y las del framework al formato ResponseDto,
 * sin exponer nunca detalles internos al cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(LoginError.class)
    public ResponseEntity<ResponseDto<Void>> handleLogin(LoginError e) {
        return build(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(ForbiddenError.class)
    public ResponseEntity<ResponseDto<Void>> handleForbidden(ForbiddenError e) {
        return build(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(NotFoundError.class)
    public ResponseEntity<ResponseDto<Void>> handleNotFound(NotFoundError e) {
        return build(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(CreationError.class)
    public ResponseEntity<ResponseDto<Void>> handleCreation(CreationError e) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(DuplicatedError.class)
    public ResponseEntity<ResponseDto<Void>> handleDuplicated(DuplicatedError e) {
        return build(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ServerError.class)
    public ResponseEntity<ResponseDto<Void>> handleServer(ServerError e) {
        log.error("Error de servidor", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseDto<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return build(HttpStatus.BAD_REQUEST, "El parámetro '" + e.getName() + "' no tiene un formato válido");
    }

    // Último recurso: nunca se devuelve el mensaje ni el stack trace de errores no controlados
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<Void>> handleUnexpected(Exception e) {
        log.error("Error no controlado", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor.");
    }

    // Errores del propio framework (JSON mal formado, método no permitido, ruta inexistente...) con el mismo formato
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
            HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        String mensaje = statusCode.is4xxClientError()
                ? HttpStatus.valueOf(statusCode.value()).getReasonPhrase()
                : "Error interno del servidor.";
        return ResponseEntity.status(statusCode).headers(headers)
                .body(new ResponseDto<Void>(statusCode.value(), mensaje, null));
    }

    private ResponseEntity<ResponseDto<Void>> build(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(new ResponseDto<>(status.value(), mensaje, null));
    }
}

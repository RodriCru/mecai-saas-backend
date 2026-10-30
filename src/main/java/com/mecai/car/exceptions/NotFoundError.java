package com.mecai.car.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundError extends RuntimeException {
    public NotFoundError(String mensaje){ super(mensaje);}
}

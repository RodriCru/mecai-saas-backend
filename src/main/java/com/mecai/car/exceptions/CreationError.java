package com.mecai.car.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class CreationError extends RuntimeException {
    public CreationError(String mensaje){ super(mensaje);}
}

package com.mecai.car.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class DuplicatedError extends RuntimeException {
    public DuplicatedError(String mensaje){ super(mensaje);}
}

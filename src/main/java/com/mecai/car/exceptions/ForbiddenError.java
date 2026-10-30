package com.mecai.car.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ForbiddenError extends RuntimeException {
    public ForbiddenError(String mensaje){ super(mensaje);}
}

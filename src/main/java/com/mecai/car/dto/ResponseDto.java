package com.mecai.car.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Es el encargado de mostrar las respuestas mas estrcuturadas y personalizadas.
 * @param <T>
 */
@Getter
@AllArgsConstructor
@Setter
public class ResponseDto<T> {
    private int codigo;
    private String mensaje;
    private T resultado;
}

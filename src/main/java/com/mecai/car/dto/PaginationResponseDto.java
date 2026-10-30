package com.mecai.car.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaginationResponseDto<T> {
    private int total;
    private int page;
    private int limit;
    private List<T> items;
}

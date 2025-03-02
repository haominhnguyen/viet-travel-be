package com.fpt.capstone.tourism.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PagingDTO<T> implements Serializable {
    private int page;
    private int size;
    private long total;
    private T items;
}

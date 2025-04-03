package com.fpt.capstone.tourism.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.Mapper;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class GeneralResponse<T> {
    @JsonProperty("code")
    private final int code;

    @JsonProperty("message")
    private final String message;

    @JsonProperty("data")
    private T data;

    public static <E> GeneralResponse<E> of(E data) {
        return GeneralResponse.<E>builder()
                .code(HttpStatus.OK.value())
                .message(HttpStatus.OK.getReasonPhrase())
                .data(data)
                .build();
    }

    public static <E> GeneralResponse<E> of(E data, String customMessage) {
        return GeneralResponse.<E>builder()
                .code(HttpStatus.OK.value())
                .message(customMessage)
                .data(data)
                .build();
    }

    public static GeneralResponse<Object> of(BusinessException be) {
        return GeneralResponse.builder()
                .code(be.getHttpCode())
                .message(be.getResponseMessage())
                .data(be.getResponseData())
                .build();
    }

    public static GeneralResponse<Object> of(Exception ex) {
        return GeneralResponse.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .data(ex.getMessage())
                .build();
    }


    public static GeneralResponse<Object> of(Exception ex, String customMessageErr) {
        return GeneralResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(customMessageErr)
                .data(ex.getMessage())
                .build();
    }

    public int getStatus() {
        return code;
    }
}
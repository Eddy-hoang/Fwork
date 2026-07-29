package com.intern.fwork.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)//Bo qua truong hop null khi chuyen qua json
public class ApiResponse<T> {

    @Builder.Default
    private int status = 200;

    private String message;

    // T co the la broad,user,task,....
    private T data;
}

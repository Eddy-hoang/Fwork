package com.intern.fwork.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommentRequest {

    @NotBlank(message = "Comment content must not be blank")
    @Size(max = 2000, message = "Comment content must not exceed 2000 characters")
    private String content;
}

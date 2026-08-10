package com.intern.fwork.controllers;

import com.intern.fwork.dtos.request.CreateLabelRequest;
import com.intern.fwork.dtos.request.UpdateLabelRequest;
import com.intern.fwork.dtos.response.ApiResponse;
import com.intern.fwork.dtos.response.LabelResponse;
import com.intern.fwork.services.LabelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @PostMapping("/api/boards/{boardId}/labels")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LabelResponse> create(
            @PathVariable UUID boardId,
            @Valid @RequestBody CreateLabelRequest request
    ) {
        return ApiResponse.success(labelService.create(boardId, request));
    }

    @GetMapping("/api/boards/{boardId}/labels")
    public ApiResponse<List<LabelResponse>> getByBoard(@PathVariable UUID boardId) {
        return ApiResponse.success(labelService.getByBoard(boardId));
    }

    @PutMapping("/api/labels/{id}")
    public ApiResponse<LabelResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLabelRequest request
    ) {
        return ApiResponse.success(labelService.update(id, request));
    }

    @DeleteMapping("/api/labels/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        labelService.delete(id);
        return ApiResponse.success(null);
    }

}

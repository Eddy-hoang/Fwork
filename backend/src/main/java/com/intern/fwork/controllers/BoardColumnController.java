package com.intern.fwork.controllers;

import com.intern.fwork.dtos.request.CreateBoardColumnRequest;
import com.intern.fwork.dtos.request.UpdateBoardColumnRequest;
import com.intern.fwork.dtos.response.ApiResponse;
import com.intern.fwork.dtos.response.BoardColumnResponse;
import com.intern.fwork.services.BoardColumnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class BoardColumnController {

    private final BoardColumnService boardColumnService;

    @PostMapping("/api/boards/{boardId}/columns")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BoardColumnResponse> create(
            @PathVariable UUID boardId,
            @Valid @RequestBody CreateBoardColumnRequest request
    ) {
        return ApiResponse.success(boardColumnService.create(boardId, request));
    }

    @GetMapping("/api/boards/{boardId}/columns")
    public ApiResponse<List<BoardColumnResponse>> getAllByBoard(@PathVariable UUID boardId) {
        return ApiResponse.success(boardColumnService.getAllByBoard(boardId));
    }

    @GetMapping("/api/columns/{id}")
    public ApiResponse<BoardColumnResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(boardColumnService.getById(id));
    }

    @RequestMapping(value = "/api/columns/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ApiResponse<BoardColumnResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBoardColumnRequest request
    ) {
        return ApiResponse.success(boardColumnService.update(id, request));
    }

    @DeleteMapping("/api/columns/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        boardColumnService.delete(id);
        return ApiResponse.success(null);
    }
}

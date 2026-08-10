package com.intern.fwork.controllers;

import com.intern.fwork.dtos.request.CreateBoardRequest;
import com.intern.fwork.dtos.request.UpdateBoardRequest;
import com.intern.fwork.dtos.response.ApiResponse;
import com.intern.fwork.dtos.response.BoardResponse;
import com.intern.fwork.services.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BoardResponse> create(@Valid @RequestBody CreateBoardRequest request) {
        return ApiResponse.success(boardService.create(request));
    }

    @GetMapping
    public ApiResponse<List<BoardResponse>> getMyBoards() {
        return ApiResponse.success(boardService.getMyBoards());
    }

    @GetMapping("/{id}")
    public ApiResponse<BoardResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(boardService.getById(id));
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ApiResponse<BoardResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBoardRequest request
    ) {
        return ApiResponse.success(boardService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        boardService.delete(id);
        return ApiResponse.success(null);
    }
}

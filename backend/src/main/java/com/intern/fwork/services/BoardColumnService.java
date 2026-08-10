package com.intern.fwork.services;

import com.intern.fwork.dtos.request.CreateBoardColumnRequest;
import com.intern.fwork.dtos.request.UpdateBoardColumnRequest;
import com.intern.fwork.dtos.response.BoardColumnResponse;

import java.util.List;
import java.util.UUID;

public interface BoardColumnService {

    BoardColumnResponse create(UUID boardId, CreateBoardColumnRequest request);

    List<BoardColumnResponse> getAllByBoard(UUID boardId);

    BoardColumnResponse getById(UUID id);

    BoardColumnResponse update(UUID id, UpdateBoardColumnRequest request);

    void delete(UUID id);

}

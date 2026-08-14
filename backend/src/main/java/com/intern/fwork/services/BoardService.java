package com.intern.fwork.services;

import com.intern.fwork.dtos.request.CreateBoardRequest;
import com.intern.fwork.dtos.request.UpdateBoardRequest;
import com.intern.fwork.dtos.response.BoardResponse;

import java.util.List;
import java.util.UUID;

public interface BoardService {

    BoardResponse create(CreateBoardRequest request);

    List<BoardResponse> getMyBoards();

    BoardResponse getById(UUID id);

    BoardResponse update(UUID id, UpdateBoardRequest request);

    void delete(UUID id);

    List<BoardResponse> getBoardsByWorkspaceId(UUID workspaceId);

    BoardResponse getBoardCacheData(UUID boardId);
}

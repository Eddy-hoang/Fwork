package com.intern.fwork.services;

import com.intern.fwork.dtos.request.CreateLabelRequest;
import com.intern.fwork.dtos.request.UpdateLabelRequest;
import com.intern.fwork.dtos.response.LabelResponse;

import java.util.List;
import java.util.UUID;

public interface LabelService {

    LabelResponse create(UUID boardId, CreateLabelRequest request);

    List<LabelResponse> getByBoard(UUID boardId);

    LabelResponse update(UUID labelId, UpdateLabelRequest request);

    void delete(UUID labelId);

    List<LabelResponse> getLabelsCacheData(UUID boardId);

}

package com.intern.fwork.services.impl;

import com.intern.fwork.dtos.request.CreateLabelRequest;
import com.intern.fwork.dtos.request.UpdateLabelRequest;
import com.intern.fwork.dtos.response.LabelResponse;
import com.intern.fwork.entities.Board;
import com.intern.fwork.entities.Label;
import com.intern.fwork.entities.Task;
import com.intern.fwork.entities.User;
import com.intern.fwork.exceptions.BoardNotFoundException;
import com.intern.fwork.exceptions.DuplicateResourceException;
import com.intern.fwork.exceptions.ResourceNotFoundException;
import com.intern.fwork.mappers.LabelMapper;
import com.intern.fwork.repositories.BoardRepository;
import com.intern.fwork.repositories.LabelRepository;
import com.intern.fwork.repositories.TaskRepository;
import com.intern.fwork.security.SecurityUtils;
import com.intern.fwork.services.LabelService;
import com.intern.fwork.services.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;
    private final BoardRepository boardRepository;
    private final TaskRepository taskRepository;
    private final LabelMapper labelMapper;
    private final SecurityUtils securityUtils;
    private final PermissionService permissionService;

    @Autowired
    @Lazy
    private LabelService self;

    @Autowired
    private CacheManager cacheManager;

    @Override
    @CacheEvict(value = "labels", key = "#boardId")
    public LabelResponse create(UUID boardId, CreateLabelRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        permissionService.checkManageLabels(boardId, currentUser.getId());

        Board board = boardRepository.findById(boardId)
                .filter(b -> !b.isArchived() && !b.getWorkspace().isArchived())
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        if (labelRepository.existsByBoardIdAndName(boardId, request.getName())) {
            throw new DuplicateResourceException("Label name already exists on this board");
        }

        Label label = Label.builder()
                .name(request.getName())
                .color(request.getColor())
                .board(board)
                .build();

        return labelMapper.toResponse(labelRepository.save(label));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabelResponse> getByBoard(UUID boardId) {
        User currentUser = securityUtils.getCurrentUser();
        Board board = boardRepository.findById(boardId)
                .filter(b -> !b.isArchived() && !b.getWorkspace().isArchived())
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        permissionService.checkWorkspaceAccess(board.getWorkspace().getId(), currentUser.getId());

        return self.getLabelsCacheData(boardId);
    }

    @Override
    public LabelResponse update(UUID labelId, UpdateLabelRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));

        if (label.getBoard().isArchived() || label.getBoard().getWorkspace().isArchived()) {
            throw new ResourceNotFoundException("Label not found");
        }

        permissionService.checkManageLabels(label.getBoard().getId(), currentUser.getId());

        if (labelRepository.existsByBoardIdAndNameAndIdNot(label.getBoard().getId(), request.getName(), labelId)) {
            throw new DuplicateResourceException("Label name already exists on this board");
        }

        label.setName(request.getName());
        label.setColor(request.getColor());

        LabelResponse response = labelMapper.toResponse(labelRepository.save(label));

        if (cacheManager.getCache("labels") != null) {
            cacheManager.getCache("labels").evict(label.getBoard().getId());
        }

        return response;
    }

    @Override
    public void delete(UUID labelId) {
        User currentUser = securityUtils.getCurrentUser();
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));

        if (label.getBoard().isArchived() || label.getBoard().getWorkspace().isArchived()) {
            throw new ResourceNotFoundException("Label not found");
        }

        permissionService.checkManageLabels(label.getBoard().getId(), currentUser.getId());

        // Decouple manually from tasks first to avoid cascade delete risk
        List<Task> tasks = taskRepository.findByLabelsId(labelId);
        for (Task task : tasks) {
            task.getLabels().remove(label);
            taskRepository.save(task);
        }

        UUID boardId = label.getBoard().getId();
        labelRepository.delete(label);

        if (cacheManager.getCache("labels") != null) {
            cacheManager.getCache("labels").evict(boardId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "labels", key = "#boardId")
    public List<LabelResponse> getLabelsCacheData(UUID boardId) {
        return labelRepository.findByBoardId(boardId).stream()
                .map(labelMapper::toResponse)
                .toList();
    }
}

package com.colearning.journal.internal;

import com.colearning.common.exception.BusinessException;
import com.colearning.common.exception.ErrorCode;
import com.colearning.common.dto.PageResponse;
import java.util.List;
import com.colearning.common.service.BaseCrudService;
import com.colearning.journal.JournalService;
import com.colearning.journal.dto.request.CreateJournalRequest;
import com.colearning.journal.dto.request.UpdateJournalRequest;
import com.colearning.journal.dto.response.JournalResponse;
import com.colearning.journal.internal.entity.Journal;
import com.colearning.journal.internal.repository.JournalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class JournalServiceImpl extends BaseCrudService<Journal, Long, CreateJournalRequest, UpdateJournalRequest, JournalResponse>
        implements JournalService {

    private final JournalRepository journalRepository;

    public JournalServiceImpl(JournalRepository journalRepository) {
        this.journalRepository = journalRepository;
    }

    @Override
    protected JpaRepository<Journal, Long> getRepository() {
        return journalRepository;
    }

    @Override
    protected Journal toEntity(Long userId, CreateJournalRequest request) {
        return Journal.builder()
                .userId(userId)
                .title(request.title())
                .contentMarkdown(request.contentMarkdown())
                .visibility(request.visibility() != null ? request.visibility() : "PRIVATE")
                .status("DRAFT")
                .build();
    }

    @Override
    protected JournalResponse toResponse(Journal entity) {
        return new JournalResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getContentMarkdown(),
                entity.getContentHtml(),
                entity.getVisibility(),
                entity.getRoomId(),
                entity.getStatus(),
                entity.getPublishedAt(),
                entity.getAiSummary(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Override
    protected void updateEntity(Journal entity, UpdateJournalRequest request) {
        if (request.title() != null) entity.setTitle(request.title());
        if (request.contentMarkdown() != null) entity.setContentMarkdown(request.contentMarkdown());
        if (request.visibility() != null) entity.setVisibility(request.visibility());
        if (request.status() != null) entity.setStatus(request.status());
        if (request.roomId() != null) entity.setRoomId(request.roomId());
    }

    @Override
    protected void validateOwnership(Journal entity, Long userId) {
        if (!entity.getUserId().equals(userId)) {
            throw BusinessException.of(ErrorCode.FORBIDDEN);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public PageResponse<JournalResponse> listMyJournals(Long userId, String status, int page, int size) {
        // TODO: 实现分页查询
        return new PageResponse<>(List.of(), page, size, 0, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JournalResponse> listPublicJournals(int page, int size) {
        // TODO: 实现分页查询
        return new PageResponse<>(List.of(), page, size, 0, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JournalResponse> listUserJournals(Long viewerId, Long targetUserId, int page, int size) {
        // TODO: 实现分页查询
        return new PageResponse<>(List.of(), page, size, 0, 0);
    }

    @Override
    @Transactional
    public JournalResponse publish(Long userId, Long journalId) {
        Journal journal = findByIdAndValidateOwnership(journalId, userId);
        journal.setStatus("PUBLISHED");
        journal = journalRepository.save(journal);
        return toResponse(journal);
    }

    @Override
    @Transactional(readOnly = true)
    public JournalResponse getPublicJournalById(Long journalId) {
        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND));
        if (!journal.isPublished() || !journal.isPublic()) {
            throw BusinessException.of(ErrorCode.NOT_FOUND);
        }
        return toResponse(journal);
    }

    private Journal findByIdAndValidateOwnership(Long journalId, Long userId) {
        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND));
        validateOwnership(journal, userId);
        return journal;
    }
}
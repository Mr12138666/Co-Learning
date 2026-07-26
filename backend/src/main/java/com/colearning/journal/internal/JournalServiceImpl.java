package com.colearning.journal.internal;

import com.colearning.common.dto.PageResponse;
import com.colearning.common.exception.BusinessException;
import com.colearning.common.exception.ErrorCode;
import com.colearning.gamification.DailyTaskService;
import com.colearning.journal.JournalService;
import com.colearning.journal.dto.request.CreateJournalRequest;
import com.colearning.journal.dto.request.UpdateJournalRequest;
import com.colearning.journal.dto.response.JournalResponse;
import com.colearning.journal.internal.entity.Journal;
import com.colearning.journal.internal.repository.JournalRepository;
import com.colearning.common.util.MarkdownSanitizer;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JournalServiceImpl implements JournalService {

    private final JournalRepository journalRepository;
    private final MarkdownSanitizer markdownSanitizer;
    private final Clock clock;
    private final DailyTaskService dailyTaskService;

    private static final Set<String> VALID_VISIBILITIES = Set.of("PRIVATE", "FRIENDS", "ROOM", "PUBLIC");
    private static final Set<String> VALID_STATUSES = Set.of("DRAFT", "PUBLISHED");

    @Override
    @Transactional
    public JournalResponse create(Long userId, CreateJournalRequest request) {
        String visibility = request.visibility() != null ? request.visibility() : "PRIVATE";
        validateVisibility(visibility);

        String contentHtml = markdownSanitizer.renderToHtml(request.contentMarkdown());

        Journal journal = Journal.builder()
                .userId(userId)
                .title(request.title())
                .contentMarkdown(request.contentMarkdown())
                .contentHtml(contentHtml)
                .visibility(visibility)
                .roomId(request.roomId())
                .status("DRAFT")
                .build();
        journal = journalRepository.save(journal);
        log.info("Journal created: userId={}, journalId={}", userId, journal.getId());
        return toResponse(journal);
    }

    @Override
    @Transactional(readOnly = true)
    public JournalResponse getById(Long userId, Long journalId) {
        Journal journal = findOwnedJournal(userId, journalId);
        return toResponse(journal);
    }

    @Override
    @Transactional
    public JournalResponse update(Long userId, Long journalId, UpdateJournalRequest request) {
        Journal journal = findOwnedJournal(userId, journalId);

        if (request.title() != null) journal.setTitle(request.title());
        if (request.contentMarkdown() != null) {
            journal.setContentMarkdown(request.contentMarkdown());
            journal.setContentHtml(markdownSanitizer.renderToHtml(request.contentMarkdown()));
        }
        if (request.visibility() != null) {
            validateVisibility(request.visibility());
            journal.setVisibility(request.visibility());
        }
        if (request.roomId() != null) journal.setRoomId(request.roomId());
        if (request.status() != null) {
            validateStatus(request.status());
            if ("PUBLISHED".equals(request.status())) {
                journal.setPublishedAt(Instant.now(clock));
            }
            journal.setStatus(request.status());
        }

        return toResponse(journal);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long journalId) {
        Journal journal = findOwnedJournal(userId, journalId);
        if (journal.isDeleted()) {
            throw BusinessException.of(ErrorCode.JOURNAL_ALREADY_DELETED);
        }
        journal.softDelete();
        log.info("Journal soft-deleted: userId={}, journalId={}", userId, journalId);
    }

    @Override
    @Transactional
    public JournalResponse publish(Long userId, Long journalId) {
        Journal journal = findOwnedJournal(userId, journalId);
        if (journal.isPublished()) {
            throw BusinessException.of(ErrorCode.JOURNAL_ALREADY_PUBLISHED);
        }
        journal.setStatus("PUBLISHED");
        journal.setPublishedAt(Instant.now(clock));
        log.info("Journal published: userId={}, journalId={}", userId, journalId);
        
        // Update daily task progress
        dailyTaskService.onWriteJournal(userId);
        
        return toResponse(journal);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JournalResponse> listMyJournals(Long userId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Journal> journals;
        if (status != null) {
            journals = journalRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
        } else {
            journals = journalRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        List<JournalResponse> items = journals.getContent().stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.of(items, page, size, journals.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JournalResponse> listPublicJournals(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Journal> journals = journalRepository.findPublicJournals(pageable);
        List<JournalResponse> items = journals.getContent().stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.of(items, page, size, journals.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public JournalResponse getPublicJournalById(Long journalId) {
        Journal journal = journalRepository.findPublicJournalById(journalId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.JOURNAL_NOT_FOUND));
        return toResponse(journal);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JournalResponse> listUserJournals(Long viewerId, Long targetUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Journal> journals = journalRepository.findVisibleJournalsByUser(targetUserId, viewerId, pageable);
        List<JournalResponse> items = journals.getContent().stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.of(items, page, size, journals.getTotalElements());
    }

    // ===== Private helpers =====

    private Journal findOwnedJournal(Long userId, Long journalId) {
        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.JOURNAL_NOT_FOUND));
        if (!journal.getUserId().equals(userId)) {
            throw BusinessException.of(ErrorCode.JOURNAL_NOT_FOUND);
        }
        return journal;
    }

    private void validateVisibility(String visibility) {
        if (!VALID_VISIBILITIES.contains(visibility)) {
            throw BusinessException.of(ErrorCode.JOURNAL_NOT_FOUND,
                    "无效的可见性: " + visibility);
        }
    }

    private void validateStatus(String status) {
        if (!VALID_STATUSES.contains(status)) {
            throw BusinessException.of(ErrorCode.JOURNAL_NOT_FOUND,
                    "无效的状态: " + status);
        }
    }

    private JournalResponse toResponse(Journal journal) {
        return new JournalResponse(
                journal.getId(),
                journal.getTitle(),
                journal.getContentMarkdown(),
                journal.getContentHtml(),
                journal.getVisibility(),
                journal.getRoomId(),
                journal.getStatus(),
                journal.getPublishedAt(),
                journal.getAiSummary(),
                journal.getCreatedAt(),
                journal.getUpdatedAt()
        );
    }
}

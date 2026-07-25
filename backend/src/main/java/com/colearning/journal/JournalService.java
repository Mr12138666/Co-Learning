package com.colearning.journal;

import com.colearning.common.dto.PageResponse;
import com.colearning.journal.dto.request.CreateJournalRequest;
import com.colearning.journal.dto.request.UpdateJournalRequest;
import com.colearning.journal.dto.response.JournalResponse;
import java.util.List;

/**
 * Service for managing private journals/diaries.
 *
 * <p>Journals support draft/publish lifecycle, soft delete, and visibility levels.
 * Markdown content is rendered to HTML on save and cached in {@code content_html}.
 */
public interface JournalService {

    JournalResponse create(Long userId, CreateJournalRequest request);

    JournalResponse getById(Long userId, Long journalId);

    JournalResponse update(Long userId, Long journalId, UpdateJournalRequest request);

    void delete(Long userId, Long journalId);

    JournalResponse publish(Long userId, Long journalId);

    PageResponse<JournalResponse> listMyJournals(Long userId, String status, int page, int size);

    PageResponse<JournalResponse> listPublicJournals(int page, int size);

    PageResponse<JournalResponse> listUserJournals(Long viewerId, Long targetUserId, int page, int size);
}

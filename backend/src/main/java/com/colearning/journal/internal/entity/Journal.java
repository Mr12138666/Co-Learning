package com.colearning.journal.internal.entity;

import com.colearning.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "journals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Journal extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 200)
    private String title;

    @Column(name = "content_markdown", nullable = false, columnDefinition = "TEXT")
    private String contentMarkdown;

    @Column(name = "content_html", columnDefinition = "TEXT")
    private String contentHtml;

    @Column(nullable = false)
    @Builder.Default
    private String visibility = "PRIVATE";  // PRIVATE | FRIENDS | ROOM | PUBLIC

    @Column(name = "room_id")
    private Long roomId;

    @Column(nullable = false)
    @Builder.Default
    private String status = "DRAFT";  // DRAFT | PUBLISHED

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    // --- Helpers ---

    public boolean isDraft() {
        return "DRAFT".equals(status);
    }

    public boolean isPublished() {
        return "PUBLISHED".equals(status);
    }

    public boolean isPrivate() {
        return "PRIVATE".equals(visibility);
    }

    public boolean isPublic() {
        return "PUBLIC".equals(visibility);
    }
}

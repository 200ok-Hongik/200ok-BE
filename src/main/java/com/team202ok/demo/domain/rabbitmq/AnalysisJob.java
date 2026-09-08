package com.team202ok.demo.domain.rabbitmq;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "ai_analysis_jobs")
@Getter
@NoArgsConstructor
public class AnalysisJob {
    @Id private String id;
    @Column(nullable = false) private Long userId;
    @Column(nullable = false) private String status;
    private String filename;
    private String contentType;
    @Lob @Column(columnDefinition = "LONGBLOB") private byte[] image;
    @Lob @Column(columnDefinition = "LONGTEXT") private String resultJson;
    private String errorMessage;
    private Instant createdAt;
    private Instant updatedAt;

    public AnalysisJob(String id, Long userId, String filename, String contentType, byte[] image) {
        this.id = id; this.userId = userId; this.filename = filename;
        this.contentType = contentType; this.image = image; this.status = "QUEUED";
        this.createdAt = this.updatedAt = Instant.now();
    }
    public void processing() { status = "PROCESSING"; updatedAt = Instant.now(); }
    public void complete(String json) { status = "COMPLETED"; resultJson = json; image = null; updatedAt = Instant.now(); }
    public void fail(String message) { status = "FAILED"; errorMessage = message; image = null; updatedAt = Instant.now(); }
}

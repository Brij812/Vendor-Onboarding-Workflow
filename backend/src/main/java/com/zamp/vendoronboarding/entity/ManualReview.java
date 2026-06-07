package com.zamp.vendoronboarding.entity;

import com.zamp.vendoronboarding.entity.enums.ReviewerOutcome;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "manual_reviews")
public class ManualReview extends TimestampedEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_run_id", nullable = false, unique = true)
    private WorkflowRun workflowRun;

    @Enumerated(EnumType.STRING)
    @Column(name = "reviewer_outcome", nullable = false)
    private ReviewerOutcome reviewerOutcome;

    @Column(name = "reviewer_note", columnDefinition = "TEXT")
    private String reviewerNote;

    @Column(name = "reviewed_at", nullable = false)
    private Instant reviewedAt;

    public WorkflowRun getWorkflowRun() {
        return workflowRun;
    }

    public void setWorkflowRun(WorkflowRun workflowRun) {
        this.workflowRun = workflowRun;
    }

    public ReviewerOutcome getReviewerOutcome() {
        return reviewerOutcome;
    }

    public void setReviewerOutcome(ReviewerOutcome reviewerOutcome) {
        this.reviewerOutcome = reviewerOutcome;
    }

    public String getReviewerNote() {
        return reviewerNote;
    }

    public void setReviewerNote(String reviewerNote) {
        this.reviewerNote = reviewerNote;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}

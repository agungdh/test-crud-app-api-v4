package id.my.agungdh.testcrudappapiv4.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Base entity for every table. Carries the internal {@code id}, public
 * {@code uuid}, audit columns and the automatic soft-delete filter.
 *
 * <p>Rules: entity classes must extend this class and must NOT redeclare any
 * field defined here. {@code id} is internal only and must never appear in
 * DTOs/JSON/URLs. Soft delete is automatic for all JPQL/Criteria queries via
 * {@code @SQLRestriction}; never call {@code repository.delete*} — use
 * {@link #softDelete(Long)} + save instead.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("deleted_at IS NULL")
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "uuid", nullable = false, updatable = false, columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID uuid;

    @CreatedDate
    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMPTZ DEFAULT now()")
    private Instant createdAt;

    @CreatedBy
    @Column(name = "created_by")
    private Long createdBy;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMPTZ DEFAULT now()")
    private Instant updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted_at", columnDefinition = "TIMESTAMPTZ")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @PrePersist
    void prePersistBase() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Marks this row as soft-deleted. Caller must save the entity afterwards.
     *
     * @param actorId internal {@code id} of the actor, or {@code null} for system actions
     */
    public void softDelete(Long actorId) {
        this.deletedAt = Instant.now();
        this.deletedBy = actorId;
    }

    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
    }
}

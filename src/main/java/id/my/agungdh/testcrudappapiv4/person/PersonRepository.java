package id.my.agungdh.testcrudappapiv4.person;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Soft-delete filter ({@code deleted_at IS NULL}) is applied automatically
 * via {@code @SQLRestriction} on the entity. All methods below only see
 * live rows. Never use {@code delete*} methods — soft-delete via the service.
 */
public interface PersonRepository extends JpaRepository<Person, Long> {

    Optional<Person> findByUuid(UUID uuid);

    Page<Person> findAll(Pageable pageable);

    /** Escape hatch for admin/restore flows: bypasses the soft-delete filter. */
    @Query(value = "SELECT * FROM person WHERE uuid = :uuid", nativeQuery = true)
    Optional<Person> findIncludingDeletedByUuid(UUID uuid);
}

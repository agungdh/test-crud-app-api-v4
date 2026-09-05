package id.my.agungdh.testcrudappapiv4.person;

import id.my.agungdh.testcrudappapiv4.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * Person. {@code male=true} is male (1), {@code male=false} is female (0).
 * BOOLEAN (1 byte) is used instead of SMALLINT (2 bytes) for space/perf.
 */
@Getter
@Setter
@Entity
@Table(name = "person")
@SQLRestriction("deleted_at IS NULL")
public class Person extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "birth_date", nullable = false, columnDefinition = "DATE")
    private LocalDate birthDate;

    @Column(name = "male", nullable = false)
    private Boolean male;
}

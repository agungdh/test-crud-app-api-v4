package id.my.agungdh.testcrudappapiv4.config;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA auditing. No authentication exists yet, so the auditor is empty
 * (all {@code *_by} columns stay {@code NULL} for system actions).
 * When auth is added, return the current user's internal {@code id} here
 * without touching any entity.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    @Bean
    AuditorAware<Long> auditorAware() {
        return () -> Optional.empty();
    }
}

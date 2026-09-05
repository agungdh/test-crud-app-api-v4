package id.my.agungdh.testcrudappapiv4.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.core.jackson.ModelResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Makes springdoc schemas/examples use SNAKE_CASE like runtime JSON.
 * springdoc (via swagger-core, still Jackson 2 while the app runs Jackson 3)
 * builds schemas with its own default mapper, so its default ModelResolver is
 * overridden with an explicitly SNAKE_CASE one.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    ModelResolver modelResolver() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        mapper.registerModule(new JavaTimeModule());
        return new ModelResolver(mapper);
    }
}

package id.my.agungdh.testcrudappapiv4;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiSnakeCaseTests {

    @Autowired
    MockMvc mockMvc;

    @Test
    void apiDocsUseSnakeCase() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("birth_date")))
                .andExpect(content().string(containsString("next_cursor")))
                .andExpect(content().string(not(containsString("birthDate"))))
                .andExpect(content().string(not(containsString("nextCursor"))));
    }
}

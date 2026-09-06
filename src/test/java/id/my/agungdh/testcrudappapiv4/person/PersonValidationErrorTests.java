package id.my.agungdh.testcrudappapiv4.person;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PersonValidationErrorTests {

    @Autowired
    MockMvc mockMvc;

    @Test
    void validationFieldIsSnakeCase() throws Exception {
        String body = """
                {"name":"x","address":"y","birth_date":"2999-01-01","male":true}""";
        mockMvc.perform(post("/api/persons").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("birth_date"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(content().string(not(org.hamcrest.Matchers.containsString("birthDate"))));
    }

    @Test
    void sortErrorListsSnakeCaseFields() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/persons")
                        .param("sort", "birthDate,desc"))
                .andExpect(status().isOk());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/persons")
                        .param("sort", "nope,desc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("birth_date")));
    }
}

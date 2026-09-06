package id.my.agungdh.testcrudappapiv4.person;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PersonListPagingTests {

    @Autowired
    MockMvc mockMvc;

    @Test
    void rejectsOversizedPage() throws Exception {
        mockMvc.perform(get("/api/persons").param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsZeroSize() throws Exception {
        mockMvc.perform(get("/api/persons").param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void defaultSizeIsAccepted() throws Exception {
        mockMvc.perform(get("/api/persons"))
                .andExpect(status().isOk());
    }
}

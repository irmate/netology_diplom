package ru.netology.cloud_backend_app;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.netology.cloud_backend_app.dto.response.ErrorResponse;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {AbstractContainerBaseTest.Initializer.class})
public class AuthenticationRestControllerIntegrationTests extends AbstractContainerBaseTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void authenticationRestControllerLoginTest() throws Exception {
        String jsonText = "{\"login\":\"test@mail.ru\", \"password\":\"test\"}";

        var response = mockMvc.perform(
                        MockMvcRequestBuilders.post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonText))
                .andExpect(MockMvcResultMatchers.status().isOk());

        var result = response.andReturn().getResponse();

        Assertions.assertNotNull(result);
    }

    @Test
    public void appBadCredentialsErrorTest() throws Exception {
        String jsonText = "{\"login\":\"test333@mail.ru\", \"password\":\"test\"}";

        var response = mockMvc.perform(
                        MockMvcRequestBuilders.post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonText))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var errorResponse = objectMapper.readValue(response, ErrorResponse.class);
        var result = errorResponse.getMessage();

        Assertions.assertEquals(result, "Bad credentials");
    }
}
package ru.netology.cloud_backend_app;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.netology.cloud_backend_app.dto.response.ErrorResponse;
import ru.netology.cloud_backend_app.dto.response.LoginResponse;
import ru.netology.cloud_backend_app.model.Content;
import ru.netology.cloud_backend_app.model.Status;
import ru.netology.cloud_backend_app.repository.ContentRepository;
import ru.netology.cloud_backend_app.repository.TokenRepository;
import ru.netology.cloud_backend_app.repository.UserRepository;

import java.util.Date;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {AbstractContainerBaseTest.Initializer.class})
class CloudBackendAppIntegrationTests extends AbstractContainerBaseTest{

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContentRepository contentRepository;

	@Autowired
	private TokenRepository tokenRepository;

	private String token;

	static MockMultipartFile mockFile;

	@BeforeAll
	public static void mockFileCreate(){
		 mockFile = new MockMultipartFile(
				"file",
				"hello.txt",
				MediaType.TEXT_PLAIN_VALUE,
				"Hello, World!".getBytes()
		);
	}

	@BeforeEach
	public void getToken() throws Exception {
		String jsonText = "{\"login\":\"test@mail.ru\", \"password\":\"test\"}";

		var authResponse = mockMvc.perform(
						MockMvcRequestBuilders.post("/login")
								.contentType(MediaType.APPLICATION_JSON)
								.content(jsonText))
				.andReturn()
				.getResponse()
				.getContentAsString();

		var loginResponse = objectMapper.readValue(authResponse, LoginResponse.class);
		token = loginResponse.getAuthToken();
	}

	@AfterEach
	public void dataBaseClean(){
		contentRepository.deleteAll();
	}

	@Test
	public void appRestControllerUploadTest() throws Exception {

		mockMvc.perform(
				MockMvcRequestBuilders.multipart("/file")
						.file(mockFile)
						.queryParam("filename", "file")
						.header("auth-token", "Bearer " + token))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void appRestControllerDeleteTest() throws Exception {

		var user = userRepository.findByLogin("test@mail.ru");
		var date = new Date();
		var content = new Content();
		content.setName(mockFile.getName());
		content.setData(mockFile.getBytes());
		content.setCreated(new Date(date.getTime()));
		content.setUpdated(new Date(date.getTime()));
		content.setStatus(Status.ACTIVE);
		content.setUser(user);

		contentRepository.saveAndFlush(content);

		mockMvc.perform(
				MockMvcRequestBuilders.delete("/file")
						.queryParam("filename", "file")
						.header("auth-token", "Bearer " + token))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void appRestControllerDownloadTest() throws Exception {

		var user = userRepository.findByLogin("test@mail.ru");
		var date = new Date();
		var content = new Content();
		content.setName(mockFile.getName());
		content.setData(mockFile.getBytes());
		content.setCreated(new Date(date.getTime()));
		content.setUpdated(new Date(date.getTime()));
		content.setStatus(Status.ACTIVE);
		content.setUser(user);

		contentRepository.saveAndFlush(content);

		var response = mockMvc.perform(
						MockMvcRequestBuilders.get("/file")
								.queryParam("filename", "file")
								.header("auth-token", "Bearer " + token))
				.andExpect(MockMvcResultMatchers.status().isOk());

		var result = response.andReturn().getResponse().getContentAsByteArray();
		Assertions.assertArrayEquals(result, mockFile.getBytes());
	}

	@Test
	public void appRestControllerEditTest() throws Exception {

		var user = userRepository.findByLogin("test@mail.ru");
		var date = new Date();
		var content = new Content();
		content.setName(mockFile.getName());
		content.setData(mockFile.getBytes());
		content.setCreated(new Date(date.getTime()));
		content.setUpdated(new Date(date.getTime()));
		content.setStatus(Status.ACTIVE);
		content.setUser(user);

		contentRepository.saveAndFlush(content);

		String editName = "{\"filename\":\"fileRev\"}";
		mockMvc.perform(
				MockMvcRequestBuilders.put("/file")
						.queryParam("filename", "file")
						.contentType(MediaType.APPLICATION_JSON)
						.content(editName)
						.header("auth-token", "Bearer " + token))
				.andExpect(MockMvcResultMatchers.status().isOk());

		Assertions.assertTrue(contentRepository.findByName("fileRev", user.getId()).isPresent());
	}

	@Test
	public void appRestControllerGetFilesByLimitTest() throws Exception {

		var user = userRepository.findByLogin("test@mail.ru");
		var date = new Date();
		var content = new Content();
		content.setName(mockFile.getName());
		content.setData(mockFile.getBytes());
		content.setCreated(new Date(date.getTime()));
		content.setUpdated(new Date(date.getTime()));
		content.setStatus(Status.ACTIVE);
		content.setUser(user);

		contentRepository.saveAndFlush(content);

		var response = mockMvc.perform(
						MockMvcRequestBuilders.get("/list")
								.queryParam("limit", "3")
								.header("auth-token", "Bearer " + token))
				.andExpect(MockMvcResultMatchers.status().isOk());

		var result = response.andReturn().getResponse().getContentAsString();
		var list = objectMapper.readValue(result, List.class);

		Assertions.assertFalse(list.isEmpty());
	}

	@Test
	public void appLogoutTest() throws Exception {

		mockMvc.perform(
				MockMvcRequestBuilders.post("/logout")
						.header("auth-token", "Bearer " + token))
				.andExpect(MockMvcResultMatchers.status().isOk());

		tokenRepository.deleteAll();
	}

	@Test
	public void appUnauthorizedErrorTest() throws Exception {

		var fakeToken = "";
		var response = mockMvc.perform(
						MockMvcRequestBuilders.multipart("/file")
								.file(mockFile)
								.queryParam("filename", "file")
								.header("auth-token", "Bearer " + fakeToken))
				.andExpect(MockMvcResultMatchers.status().is4xxClientError())
				.andReturn()
				.getResponse()
				.getContentAsString();

		var errorResponse = objectMapper.readValue(response, ErrorResponse.class);
		var result = errorResponse.getMessage();

		Assertions.assertEquals(result, "Unauthorized error");
	}

	@Test
	public void appInputDataErrorTest() throws Exception {

		var response = mockMvc.perform(
						MockMvcRequestBuilders.multipart("/file")
								.queryParam("filename", "file")
								.header("auth-token", "Bearer " + token))
				.andExpect(MockMvcResultMatchers.status().is4xxClientError())
				.andReturn()
				.getResponse()
				.getContentAsString();

		var errorResponse = objectMapper.readValue(response, ErrorResponse.class);
		var result = errorResponse.getMessage();

		Assertions.assertEquals(result, "Error input data");

	}

	@Test
	public void appDeleteFileErrorTest() throws Exception {

		var response = mockMvc.perform(
						MockMvcRequestBuilders.delete("/file")
								.queryParam("filename", "file")
								.header("auth-token", "Bearer " + token))
				.andExpect(MockMvcResultMatchers.status().is5xxServerError())
				.andReturn()
				.getResponse()
				.getContentAsString();

		var errorResponse = objectMapper.readValue(response, ErrorResponse.class);
		var result = errorResponse.getMessage();

		Assertions.assertEquals(result, "Error delete file");
	}

	@Test
	public void appDownloadFileErrorTest() throws Exception {

		var response = mockMvc.perform(
						MockMvcRequestBuilders.get("/file")
								.queryParam("filename", "file")
								.header("auth-token", "Bearer " + token))
				.andExpect(MockMvcResultMatchers.status().is5xxServerError())
				.andReturn()
				.getResponse()
				.getContentAsString();

		var errorResponse = objectMapper.readValue(response, ErrorResponse.class);
		var result = errorResponse.getMessage();

		Assertions.assertEquals(result, "Error download file");
	}

	@Test
	public void appEditFilenameErrorTest() throws Exception {

		String editName = "{\"filename\":\"fileRev\"}";
		var response = mockMvc.perform(
						MockMvcRequestBuilders.put("/file")
								.queryParam("filename", "file")
								.contentType(MediaType.APPLICATION_JSON)
								.content(editName)
								.header("auth-token", "Bearer " + token))
				.andExpect(MockMvcResultMatchers.status().is5xxServerError())
				.andReturn()
				.getResponse()
				.getContentAsString();

		var errorResponse = objectMapper.readValue(response, ErrorResponse.class);
		var result = errorResponse.getMessage();

		Assertions.assertEquals(result, "Error edit filename");
	}
}
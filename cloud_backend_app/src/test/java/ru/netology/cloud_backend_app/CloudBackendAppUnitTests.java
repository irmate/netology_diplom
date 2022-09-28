package ru.netology.cloud_backend_app;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import ru.netology.cloud_backend_app.exception.ContentNotFoundException;
import ru.netology.cloud_backend_app.model.Role;
import ru.netology.cloud_backend_app.model.Status;
import ru.netology.cloud_backend_app.model.Token;
import ru.netology.cloud_backend_app.repository.ContentRepository;
import ru.netology.cloud_backend_app.repository.TokenRepository;
import ru.netology.cloud_backend_app.security.authentication.AppAuthenticationManager;
import ru.netology.cloud_backend_app.security.handler.AppLogoutHandler;
import ru.netology.cloud_backend_app.security.jwt.JwtTokenProvider;
import ru.netology.cloud_backend_app.security.jwt.JwtUserDetailsService;
import ru.netology.cloud_backend_app.security.jwt.JwtUserFactory;
import ru.netology.cloud_backend_app.service.BlacklistService;
import ru.netology.cloud_backend_app.service.UserService;
import ru.netology.cloud_backend_app.service.impl.ContentManagerServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {AbstractContainerBaseTest.Initializer.class})
public class CloudBackendAppUnitTests extends AbstractContainerBaseTest {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private BlacklistService blacklistService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private ContentRepository contentRepository;

    @Test
    public void appAuthenticationManagerAuthenticateTest(){
        var testLogin = "test@mail.ru";
        var testPassword = "test";
        var jwtUserDetailsService = new JwtUserDetailsService(userService);

        var appAuthenticationManager = new AppAuthenticationManager(passwordEncoder, jwtUserDetailsService);
        var result = appAuthenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(testLogin, testPassword)
        );

        Assertions.assertNotNull(result);
    }

    @Test
    public void appAuthenticationManagerAuthenticateExceptionTest(){
        var testLogin = "test@mail.ru";
        var testPassword = "wrong_password";
        var jwtUserDetailsService = new JwtUserDetailsService(userService);

        var appAuthenticationManager = new AppAuthenticationManager(passwordEncoder, jwtUserDetailsService);

        Assertions.assertThrows(
                BadCredentialsException.class,
                ()-> appAuthenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(testLogin, testPassword))
        );
    }

    @Test
    public void appLogoutHandlerLogoutTest(){
        var jwtTokenProvider = Mockito.mock(JwtTokenProvider.class);
        var request = Mockito.mock(HttpServletRequest.class);
        var response = Mockito.mock(HttpServletResponse.class);
        var authentication = Mockito.mock(Authentication.class);

        Mockito.when(jwtTokenProvider.resolveToken(request))
                .thenReturn("test.token");
        Mockito.when(jwtTokenProvider.getUserLogin(Mockito.anyString()))
                .thenReturn("test@mail.ru");
        Mockito.when(jwtTokenProvider.getBlacklistService())
                .thenReturn(blacklistService);

        var appLogoutHandler = new AppLogoutHandler(jwtTokenProvider, userService);
        appLogoutHandler.logout(request, response, authentication);

        Assertions.assertTrue(tokenRepository.findByName("test.token").isPresent());
    }

    @Test
    public void jwtTokenProviderCreateTokenTest(){
        var testLogin = "test@mail.ru";
        var testRoles = List.of(Role.builder().name("ROLE_USER").build());

        var jwtTokenProvider = new JwtTokenProvider(userDetailsService, blacklistService);
        var result = jwtTokenProvider.createToken(testLogin, testRoles);

        Assertions.assertNotNull(result);
    }

    @Test
    public void jwtTokenProviderGetAuthenticationTest() {
        var testLogin = "test@mail.ru";
        var testRoles = List.of(Role.builder().name("ROLE_USER").build());

        var jwtTokenProvider = new JwtTokenProvider(userDetailsService, blacklistService);
        var testToken = jwtTokenProvider.createToken(testLogin, testRoles);
        var result = jwtTokenProvider.getAuthentication(testToken);

        Assertions.assertNotNull(result);
    }

    @Test
    public void jwtTokenProviderGetUserLoginTest(){
        var testLogin = "test@mail.ru";
        var testRoles = List.of(Role.builder().name("ROLE_USER").build());

        var jwtTokenProvider = new JwtTokenProvider(userDetailsService, blacklistService);
        var token = jwtTokenProvider.createToken(testLogin, testRoles);
        var result = jwtTokenProvider.getUserLogin(token);

        Assertions.assertEquals(testLogin, result);
    }

    @Test
    public void jwtTokenProviderResolveTokenTest(){
        var expected = "test.token";

        var request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader(Mockito.anyString()))
                .thenReturn("Bearer " + expected);

        var jwtTokenProvider = new JwtTokenProvider(userDetailsService, blacklistService);
        var result = jwtTokenProvider.resolveToken(request);

        Assertions.assertEquals(expected, result);
    }

    @Test
    public void jwtTokenProviderValidateTokenTest(){
        var testLogin = "test@mail.ru";
        var testRoles = List.of(Role.builder().name("ROLE_USER").build());

        var jwtTokenProvider = new JwtTokenProvider(userDetailsService, blacklistService);
        var token = jwtTokenProvider.createToken(testLogin, testRoles);

        var result = jwtTokenProvider.validateToken(token);

        Assertions.assertTrue(result);
    }

    @Test
    public void jwtTokenProviderGetRolesNamesTest(){
        var expected = List.of("ROLE_USER");
        var testRoles = List.of(Role.builder().name("ROLE_USER").build());

        var jwtTokenProvider = new JwtTokenProvider(userDetailsService, blacklistService);
        var result = jwtTokenProvider.getRoleNames(testRoles);

        Assertions.assertEquals(expected, result);
    }

    @Test
    public void jwtUserFactoryCreateTest() {
        var user = userService.findByLogin("test@mail.ru");
        var result = JwtUserFactory.create(user);

        Assertions.assertNotNull(result);
    }

    @Test
    public void blackListServiceAddTokenToBlackListTestAndCheckTokenAtBlacklistTest(){
        var tokenName = "test.token";
        var user = userService.findByLogin("test@mail.ru");
        var date = new Date();
        var expected = new Token();
        expected.setName(tokenName);
        expected.setCreated(new Date(date.getTime()));
        expected.setUpdated(new Date(date.getTime()));
        expected.setStatus(Status.ACTIVE);
        expected.setUser(user);

        blacklistService.addTokenToBlacklist(expected);

        var result = blacklistService.checkTokenAtBlacklist(tokenName);

        Assertions.assertTrue(result);
    }

    @Test
    public void blackListServiceUpdateStatusOfTokenTest(){
        var tokenName = "test.token2";
        var user = userService.findByLogin("test@mail.ru");
        var date = new Date();
        var expected = new Token();
        expected.setName(tokenName);
        expected.setCreated(new Date(date.getTime()));
        expected.setUpdated(new Date(date.getTime()));
        expected.setStatus(Status.ACTIVE);
        expected.setUser(user);

        blacklistService.addTokenToBlacklist(expected);
        blacklistService.updateStatusOfToken(user.getId());

        var result = blacklistService.checkTokenAtBlacklist(tokenName);

        Assertions.assertFalse(result);
    }

    @Test
    public void userServiceFindByLoginTest(){
        var testLogin = "test@mail.ru";
        var result = userService.findByLogin(testLogin);

        Assertions.assertNotNull(result);
    }

    @Test
    public void contentManagerServiceUploadAndDeleteTest(){
        var testLogin = "test@mail.ru";
        var testPassword = "test";
        var jwtUserDetailsService = new JwtUserDetailsService(userService);

        var appAuthenticationManager = new AppAuthenticationManager(passwordEncoder, jwtUserDetailsService);
        var auth = appAuthenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(testLogin, testPassword)
        );

        var filename = "file";
        var mockFile = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        var contentManagerService = new ContentManagerServiceImpl(contentRepository, userService);
        contentManagerService.upload(filename, mockFile, auth);
        var resultUpload = contentRepository.findByName(filename, userService.findByLogin(auth.getName()).getId());

        Assertions.assertTrue(resultUpload.isPresent());

        contentManagerService.delete(filename, auth);
        var resultDelete = contentRepository.findByName(filename, userService.findByLogin(auth.getName()).getId());

        Assertions.assertFalse(resultDelete.isPresent());

        contentRepository.deleteAll();
    }

    @Test
    public void contentManagerServiceGetFileTest(){
        var testLogin = "test@mail.ru";
        var testPassword = "test";
        var jwtUserDetailsService = new JwtUserDetailsService(userService);

        var appAuthenticationManager = new AppAuthenticationManager(passwordEncoder, jwtUserDetailsService);
        var auth = appAuthenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(testLogin, testPassword)
        );

        var filename = "file";
        var mockFile = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        var contentManagerService = new ContentManagerServiceImpl(contentRepository, userService);
        contentManagerService.upload(filename, mockFile, auth);

        var result = contentManagerService.getFile(filename, auth);

        Assertions.assertNotNull(result);

        contentRepository.deleteAll();
    }

    @Test
    public void contentManagerServiceEditTest(){
        var testLogin = "test@mail.ru";
        var testPassword = "test";
        var jwtUserDetailsService = new JwtUserDetailsService(userService);

        var appAuthenticationManager = new AppAuthenticationManager(passwordEncoder, jwtUserDetailsService);
        var auth = appAuthenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(testLogin, testPassword)
        );

        var filename = "file";
        var mockFile = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        var contentManagerService = new ContentManagerServiceImpl(contentRepository, userService);
        contentManagerService.upload(filename, mockFile, auth);

        var editFilename = "Edit.file";
        contentManagerService.edit(editFilename, filename, auth);

        var result = contentRepository.findByName(editFilename, userService.findByLogin(auth.getName()).getId());

        Assertions.assertTrue(result.isPresent());

        contentRepository.deleteAll();
    }

    @Test
    public void contentManagerServiceGetListByLimitTest(){
        var testLogin = "test@mail.ru";
        var testPassword = "test";
        var jwtUserDetailsService = new JwtUserDetailsService(userService);

        var appAuthenticationManager = new AppAuthenticationManager(passwordEncoder, jwtUserDetailsService);
        var auth = appAuthenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(testLogin, testPassword)
        );

        var filename = "file";
        var mockFile = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        var contentManagerService = new ContentManagerServiceImpl(contentRepository, userService);
        contentManagerService.upload(filename, mockFile, auth);

        var result = contentManagerService.getListByLimit(3, auth);

        Assertions.assertFalse(result.isEmpty());

        contentRepository.deleteAll();
    }

    @Test
    public void contentManagerServiceDeleteExceptionTest(){
        var testLogin = "test@mail.ru";
        var testPassword = "test";
        var jwtUserDetailsService = new JwtUserDetailsService(userService);

        var appAuthenticationManager = new AppAuthenticationManager(passwordEncoder, jwtUserDetailsService);
        var auth = appAuthenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(testLogin, testPassword)
        );

        var filename = "file";
        var contentManagerService = new ContentManagerServiceImpl(contentRepository, userService);
        Assertions.assertThrows(ContentNotFoundException.class, () -> contentManagerService.delete(filename, auth));
    }

    @Test
    public void contentManagerServiceGetFileExceptionTest(){
        var testLogin = "test@mail.ru";
        var testPassword = "test";
        var jwtUserDetailsService = new JwtUserDetailsService(userService);

        var appAuthenticationManager = new AppAuthenticationManager(passwordEncoder, jwtUserDetailsService);
        var auth = appAuthenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(testLogin, testPassword)
        );

        var filename = "file";
        var contentManagerService = new ContentManagerServiceImpl(contentRepository, userService);
        Assertions.assertThrows(ContentNotFoundException.class, () -> contentManagerService.getFile(filename, auth));
    }

    @Test
    public void contentManagerServiceEditExceptionTest(){
        var testLogin = "test@mail.ru";
        var testPassword = "test";
        var jwtUserDetailsService = new JwtUserDetailsService(userService);

        var appAuthenticationManager = new AppAuthenticationManager(passwordEncoder, jwtUserDetailsService);
        var auth = appAuthenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(testLogin, testPassword)
        );

        var filename = "file";
        var editFilename = "Edit.file";
        var contentManagerService = new ContentManagerServiceImpl(contentRepository, userService);
        Assertions.assertThrows(ContentNotFoundException.class, () -> contentManagerService.edit(editFilename, filename, auth));
    }
}
package ru.netology.cloud_backend_app.security.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import ru.netology.cloud_backend_app.dto.response.ErrorResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Random;

@Component
public class AppAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    private ObjectMapper mapper;

    public AppAuthenticationEntryPoint() {
    }

    @Override
    public void commence(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            AuthenticationException e)
            throws IOException {

        httpServletResponse.setContentType("application/json");
        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpServletResponse
                .getOutputStream()
                .write(mapper.writeValueAsString(
                        new ErrorResponse(
                                "Unauthorized error",
                                new Random().nextInt(1000))).getBytes()
                );
    }
}
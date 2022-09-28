package ru.netology.cloud_backend_app.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import ru.netology.cloud_backend_app.dto.response.ErrorResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Random;

@Component
public class JwtTokenFilter extends GenericFilterBean {

    @Autowired
    private ObjectMapper mapper;

    private final JwtTokenProvider jwtTokenProvider;

    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain filterChain)
            throws IOException, ServletException
    {
        ServletServerHttpResponse res = new ServletServerHttpResponse((HttpServletResponse) servletResponse);

        try {
            var token = jwtTokenProvider.resolveToken((HttpServletRequest) servletRequest);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                var authentication = jwtTokenProvider.getAuthentication(token);

                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            filterChain.doFilter(servletRequest, servletResponse);

        } catch (SignatureException | IllegalArgumentException e) {
            res.setStatusCode(HttpStatus.UNAUTHORIZED);
            res.getServletResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            res.getBody().write(
                    mapper.writeValueAsString(
                            new ErrorResponse(
                                    "Unauthorized error",
                                    new Random().nextInt(1000))).getBytes()
            );
        }
    }
}
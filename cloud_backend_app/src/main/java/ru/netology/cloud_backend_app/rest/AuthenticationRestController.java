package ru.netology.cloud_backend_app.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import ru.netology.cloud_backend_app.dto.request.AuthenticationRequest;
import ru.netology.cloud_backend_app.dto.response.LoginResponse;
import ru.netology.cloud_backend_app.security.jwt.JwtTokenProvider;
import ru.netology.cloud_backend_app.service.UserService;

@RestController
@RequestMapping("/")
public class AuthenticationRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    public AuthenticationRestController(
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            UserService userService)
    {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody AuthenticationRequest requestDto) {
        var login = requestDto.getLogin();
        var password = requestDto.getPassword();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, password));
        var user = userService.findByLogin(login);
        var token = jwtTokenProvider.createToken(login, user.getRoles());
        return new ResponseEntity<>(new LoginResponse(token), HttpStatus.OK);
    }
}
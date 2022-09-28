package ru.netology.cloud_backend_app.security.authentication;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.netology.cloud_backend_app.security.jwt.JwtUserDetailsService;

@Component
public class AppAuthenticationManager implements AuthenticationManager {

    private final JwtUserDetailsService jwtUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    public AppAuthenticationManager(PasswordEncoder passwordEncoder, JwtUserDetailsService jwtUserDetailsService) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUserDetailsService = jwtUserDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var login = authentication.getName();
        var password = authentication.getCredentials().toString();
        var userDetails = jwtUserDetailsService.loadUserByUsername(login);

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Bad password");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
    }
}
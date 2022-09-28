package ru.netology.cloud_backend_app.security.jwt;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.netology.cloud_backend_app.service.UserService;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    private final UserService userService;

    public JwtUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        var user = userService.findByLogin(login);
        if(user ==null){
            throw new UsernameNotFoundException("User with login: " + login + "not found");
        }
        return JwtUserFactory.create(user);
    }
}
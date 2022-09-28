package ru.netology.cloud_backend_app.security.jwt;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.netology.cloud_backend_app.model.Role;
import ru.netology.cloud_backend_app.model.Status;
import ru.netology.cloud_backend_app.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class JwtUserFactory {
    public JwtUserFactory() {
    }

    public static JwtUser create(User user){
        return new JwtUser(
                        user.getId(),
                        user.getLogin(),
                        user.getPassword(),
                user.getStatus().equals(Status.ACTIVE),
                user.getUpdated(),
                mapToGrantedAuthorities(new ArrayList<>(user.getRoles()))
                );
    }

    private static List<GrantedAuthority> mapToGrantedAuthorities(List<Role> userRoles){
        return userRoles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }
}
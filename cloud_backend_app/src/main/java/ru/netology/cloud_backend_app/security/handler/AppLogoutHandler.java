package ru.netology.cloud_backend_app.security.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import ru.netology.cloud_backend_app.model.Status;
import ru.netology.cloud_backend_app.model.Token;
import ru.netology.cloud_backend_app.security.jwt.JwtTokenProvider;
import ru.netology.cloud_backend_app.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

public class AppLogoutHandler implements LogoutHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    public AppLogoutHandler(JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        var tokenName = jwtTokenProvider.resolveToken(request);
        var login = jwtTokenProvider.getUserLogin(tokenName);
        var user = userService.findByLogin(login);

        var date = new Date();
        var token = new Token();
        token.setName(tokenName);
        token.setCreated(new Date(date.getTime()));
        token.setUpdated(new Date(date.getTime()));
        token.setStatus(Status.ACTIVE);
        token.setUser(user);

        var blacklistService = jwtTokenProvider.getBlacklistService();
        blacklistService.updateStatusOfToken(user.getId());
        blacklistService.addTokenToBlacklist(token);
    }
}

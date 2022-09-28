package ru.netology.cloud_backend_app.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import ru.netology.cloud_backend_app.model.Role;
import ru.netology.cloud_backend_app.service.BlacklistService;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;

@Data
@Component
public class JwtTokenProvider {

    private Key key;

    private final UserDetailsService userDetailsService;
    private final BlacklistService blacklistService;

    public JwtTokenProvider(UserDetailsService userDetailsService, BlacklistService blacklistService) {
        this.userDetailsService = userDetailsService;
        this.blacklistService = blacklistService;
    }

    @PostConstruct
    protected void init() {
        key = Keys.secretKeyFor(HS256);
    }

    public String createToken(String login, List<Role> roles) {
        Claims claims = Jwts.claims().setSubject(login);
        claims.put("roles", getRoleNames(roles));

        Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiration = issuedAt.plus(3, ChronoUnit.MINUTES);

        if(key==null){
            init();
        }

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        var username = getUserLogin(token);
        var userDetails = this.userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUserLogin(String token) {
        if(key==null){
            init();
        }
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    public String resolveToken(HttpServletRequest req) {
        var bearerToken = req.getHeader("auth-token");
        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token) {
        Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        return
                !claims.getBody().getExpiration().before(new Date())
                        && !blacklistService.checkTokenAtBlacklist(token);

    }

    public List<String> getRoleNames(List<Role> userRoles) {
        List<String> result = new ArrayList<>();
        userRoles.forEach(role -> result.add(role.getName()));
        return result;
    }
}
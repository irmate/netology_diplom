package ru.netology.cloud_backend_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import ru.netology.cloud_backend_app.security.authentication.AppAuthenticationEntryPoint;
import ru.netology.cloud_backend_app.security.handler.AppLogoutHandler;
import ru.netology.cloud_backend_app.security.jwt.JwtTokenFilter;
import ru.netology.cloud_backend_app.security.jwt.JwtTokenProvider;
import ru.netology.cloud_backend_app.service.UserService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig implements WebMvcConfigurer {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final JwtTokenFilter jwtTokenFilter;
    private final AppAuthenticationEntryPoint authenticationEntryPoint;

    private static final String LOGIN_ENDPOINT = "/login";
    private static final String LOGOUT_ENDPOINT = "/logout";

    public SecurityConfig(
            JwtTokenProvider jwtTokenProvider,
            UserService userService,
            JwtTokenFilter jwtTokenFilter,
            AppAuthenticationEntryPoint authenticationEntryPoint
    )
    {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.jwtTokenFilter = jwtTokenFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public PasswordEncoder getPasswordEncoder(){
        return new BCryptPasswordEncoder(4);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .logout()
                    .logoutUrl(LOGOUT_ENDPOINT)
                    .addLogoutHandler(new AppLogoutHandler(jwtTokenProvider, userService))
                    .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .addLogoutHandler(new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(ClearSiteDataHeaderWriter.Directive.ALL)))
                .and()
                    .authorizeRequests()
                    .antMatchers(LOGIN_ENDPOINT).permitAll()
                    .anyRequest().hasRole("USER")
                .and()
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:8080")
                        .allowedMethods("*")
                        .allowCredentials(true);
            }
        };
    }
}
package io.github.mido.kenning.config;

import io.github.mido.kenning.user.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfig {

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/documents/**", "/api/conversations/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(customOAuth2UserService))
                        .defaultSuccessUrl(frontendBaseUrl)
                )
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class);

        return http.build();
    }
}

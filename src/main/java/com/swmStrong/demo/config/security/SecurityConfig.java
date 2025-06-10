package com.swmStrong.demo.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmStrong.demo.config.security.filter.CustomAuthenticationFilter;
import com.swmStrong.demo.config.security.filter.TokenAuthorizationFilter;
import com.swmStrong.demo.config.security.handler.CustomAuthenticationFailureHandler;
import com.swmStrong.demo.config.security.handler.CustomAuthenticationSuccessHandler;
import com.swmStrong.demo.config.security.handler.CustomLogoutHandler;
import com.swmStrong.demo.config.security.handler.CustomLogoutSuccessHandler;
import com.swmStrong.demo.config.security.provider.CustomAuthenticationProvider;
import com.swmStrong.demo.infra.redis.repository.RedisRepositoryImpl;
import com.swmStrong.demo.infra.token.TokenType;
import com.swmStrong.demo.infra.token.TokenManager;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true)
public class SecurityConfig {

    private final RedisRepositoryImpl redisRepository;
    private final TokenManager tokenManager;
    private final ObjectMapper objectMapper;

    public SecurityConfig(RedisRepositoryImpl redisRepository, TokenManager tokenManager, ObjectMapper objectMapper) {
        this.redisRepository = redisRepository;
        this.tokenManager = tokenManager;
        this.objectMapper = objectMapper;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public SecurityFilterChain whiteListFilterChain(
            HttpSecurity http
    ) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .securityMatcher(WhiteListConfig.WHITE_LIST)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WhiteListConfig.WHITE_LIST).permitAll()
                )
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomAuthenticationFilter customAuthenticationFilter,
            TokenAuthorizationFilter tokenAuthorizationFilter,
            CustomLogoutSuccessHandler customLogoutSuccessHandler,
            CustomLogoutHandler customLogoutHandler
    ) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, WhiteListConfig.WHITE_LIST_FOR_GET).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(tokenAuthorizationFilter, LogoutFilter.class)
                .addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tokenAuthorizationFilter, BasicAuthenticationFilter.class)
                .logout(logout -> logout
                        .addLogoutHandler(customLogoutHandler)
                        .logoutUrl("/auth/logout")
                        .clearAuthentication(true)
                        .deleteCookies(TokenType.accessToken.toString(), TokenType.refreshToken.toString())
                        .invalidateHttpSession(true)
                        .logoutSuccessHandler(customLogoutSuccessHandler)
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("https://mvp-web-view.vercel.app");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            CustomAuthenticationProvider customAuthenticationProvider
    ) {
        return new ProviderManager(Collections.singletonList(customAuthenticationProvider));
    }

    @Bean
    public CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler(
                tokenManager,
                objectMapper
        );
    }

    @Bean
    public CustomAuthenticationFailureHandler customAuthenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler(
                objectMapper
        );
    }

    @Bean
    public CustomAuthenticationFilter customAuthenticationFilter(
            AuthenticationManager authenticationManager,
            CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler,
            CustomAuthenticationFailureHandler customAuthenticationFailureHandler
    ) {
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManager, objectMapper);
        customAuthenticationFilter.setFilterProcessesUrl("/auth/login");
        customAuthenticationFilter.setAuthenticationSuccessHandler(customAuthenticationSuccessHandler);
        customAuthenticationFilter.setAuthenticationFailureHandler(customAuthenticationFailureHandler);
        return customAuthenticationFilter;
    }

    @Bean
    public TokenAuthorizationFilter tokenAuthorizationFilter() {
        return new TokenAuthorizationFilter(tokenManager);
    }

    @Bean
    public FilterRegistrationBean<TokenAuthorizationFilter> disableTokenAuthFilter(TokenAuthorizationFilter filter) {
        FilterRegistrationBean<TokenAuthorizationFilter> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }

    @Bean
    public CustomLogoutHandler customLogoutHandler() {
        return new CustomLogoutHandler(redisRepository);
    }

    @Bean
    public CustomLogoutSuccessHandler customLogoutSuccessHandler() {
        return new CustomLogoutSuccessHandler();
    }
}
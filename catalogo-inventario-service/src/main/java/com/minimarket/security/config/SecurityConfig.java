package com.minimarket.security.config;

import com.minimarket.security.audit.SecurityAuditHandler;
import com.minimarket.security.filter.JwtRequestFilter;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SecurityAuditHandler securityAuditHandler;
    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(
            SecurityAuditHandler securityAuditHandler,
            JwtRequestFilter jwtRequestFilter) {
        this.securityAuditHandler = securityAuditHandler;
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    @Order(1)
    @Profile("dev")
    public SecurityFilterChain h2ConsoleSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(PathRequest.toH2Console())
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(securityAuditHandler)
                .accessDeniedHandler(securityAuditHandler)
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/internal/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/categorias/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/inventario/**").hasAnyRole("EMPLEADO", "GERENTE", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/inventario/salidas").hasAnyRole("EMPLEADO", "GERENTE", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/inventario/entradas").hasAnyRole("GERENTE", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/inventario/**").hasAnyRole("GERENTE", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/inventario/**").hasAnyRole("GERENTE", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/inventario/**").hasAnyRole("GERENTE", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/productos/**", "/api/categorias/**").hasAnyRole("GERENTE", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/productos/**", "/api/categorias/**").hasAnyRole("GERENTE", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/**", "/api/categorias/**").hasAnyRole("GERENTE", "ADMIN")
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

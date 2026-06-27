package com.minimarket.security.config;

import com.minimarket.security.audit.SecurityAuditHandler;
import com.minimarket.security.filter.JwtRequestFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SecurityAuditHandler securityAuditHandler;
    private final JwtRequestFilter jwtRequestFilter;
    private final LoginRateLimitFilter loginRateLimitFilter;

    public SecurityConfig(
            SecurityAuditHandler securityAuditHandler,
            JwtRequestFilter jwtRequestFilter,
            LoginRateLimitFilter loginRateLimitFilter) {
        this.securityAuditHandler = securityAuditHandler;
        this.jwtRequestFilter = jwtRequestFilter;
        this.loginRateLimitFilter = loginRateLimitFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
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
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/categorias/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/carrito/checkout/*")
                    .hasAnyRole("EMPLEADO", "GERENTE", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/carrito/checkout").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/carrito")
                    .hasAnyRole("CLIENTE", "EMPLEADO", "GERENTE", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/carrito/todos", "/api/carrito/*")
                    .hasAnyRole("EMPLEADO", "GERENTE", "ADMIN")
                .requestMatchers("/api/carrito/**").hasAnyRole("CLIENTE", "ADMIN")
                .requestMatchers("/api/ventas/**", "/api/detalle-ventas/**").hasAnyRole("EMPLEADO", "GERENTE", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/inventario/**").hasAnyRole("EMPLEADO", "GERENTE", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/inventario/**").hasAnyRole("GERENTE", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/inventario/**").hasAnyRole("GERENTE", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/inventario/**").hasAnyRole("GERENTE", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/productos/**", "/api/categorias/**").hasAnyRole("GERENTE", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/productos/**", "/api/categorias/**").hasAnyRole("GERENTE", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/**", "/api/categorias/**").hasAnyRole("GERENTE", "ADMIN")
                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            );

        http.addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

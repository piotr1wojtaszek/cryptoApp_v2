package com.piwo.cryptoApp.security;

import com.piwo.cryptoApp.model.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {


    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/api/v1/cryptoApp/registration/**").permitAll()
                        .requestMatchers("/api/v1/cryptoApp/exchange/**").permitAll()
                        .requestMatchers("/api/v1/cryptoApp/wallet/**").hasRole(Role.USER.name())
                        .requestMatchers("/api/v1/cryptoApp/trades/**").hasAnyRole(Role.USER.name(),Role.ADMIN.name())
                        .requestMatchers("/api/v1/cryptoApp/account/**").hasRole(Role.ADMIN.name())
                        .anyRequest().authenticated()
                )
                .httpBasic();
        return http.build();
    }
}

package com.externship.appointment.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Allow all requests unless explicitly restricted
                )
                .csrf().disable()  // Disable CSRF for testing (enable later if needed)
                .formLogin().disable() // Disable default login form
                .logout().logoutUrl("/logout").logoutSuccessUrl("/"); // Handle logout

        return http.build();
    }
}

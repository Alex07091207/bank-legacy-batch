package com.banco.bank_legacy_batch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/bff/web/**").hasRole("WEB")
                .requestMatchers("/bff/mobile/**").hasRole("MOBILE")
                .requestMatchers("/bff/atm/**").hasRole("ATM")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails webUser = User.builder()
                .username("web_client")
                .password(passwordEncoder.encode("WebPass2026!"))
                .roles("WEB")
                .build();
        UserDetails mobileUser = User.builder()
                .username("mobile_client")
                .password(passwordEncoder.encode("MobilePass2026!"))
                .roles("MOBILE")
                .build();
        UserDetails atmUser = User.builder()
                .username("atm_client")
                .password(passwordEncoder.encode("AtmPass2026!"))
                .roles("ATM")
                .build();
        return new InMemoryUserDetailsManager(webUser, mobileUser, atmUser);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
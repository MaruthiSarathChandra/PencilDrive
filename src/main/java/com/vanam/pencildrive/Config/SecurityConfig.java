package com.vanam.pencildrive.Config;
import com.vanam.pencildrive.security.JwtAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;





@Configuration
public class SecurityConfig {


    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        // Allow Spring to process internal error dispatches
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                        .requestMatchers("/api/auth/register",
                                "/api/auth/login",
                                "/api/login", "/static/**").permitAll()
                        .anyRequest().authenticated()
                ).exceptionHandling(ex-> ex
                        .authenticationEntryPoint(((request, response, authException) -> {
                                response.setStatus(401);
                                response.setContentType("application/json");

                                response.getWriter().write("""
                                        { 
                                        "status": 401,
                                        "error": "UNAUTHORIZED",
                                        "message": "Authentication required"
                                        }
                                        """);
                        }))

                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");

                            response.getWriter().write("""
                                    {
                                    "status": 403,
                                    "error": "FORBIDDEN",
                                    "message": "Access denied"
                                    }
                                    """);
                        })

                        /*.authenticationEntryPoint(((request, response, authException) -> {
                            response.sendRedirect("/api/login");
                        }))*/
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

package org.example.onlinevotingsystem.auth;

import org.example.onlinevotingsystem.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true) // @RolesAllowed("")
public class WebSecurityConfig {

	private final AuthenticationFailureHandler customAuthenticationFailureHandler;
	private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
	private final UserService userDetailsService;

	@Autowired
	public WebSecurityConfig(AuthenticationFailureHandler customAuthenticationFailureHandler,
			UserService userDetailsService, CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) { // Add
																												// this
		this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
		this.userDetailsService = userDetailsService;
		this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(
						authorize -> authorize.requestMatchers("/register", "/login", "/css/**", "/js/**").permitAll()
								.requestMatchers("/admin/**").hasRole("ADMIN").anyRequest().authenticated())
				.formLogin(form -> form.loginPage("/login").successHandler(customAuthenticationSuccessHandler)
						.failureHandler(customAuthenticationFailureHandler))
				.logout(logout -> logout.logoutSuccessUrl("/login?logout=true")).httpBasic(Customizer.withDefaults())
				.exceptionHandling(exception -> exception.accessDeniedPage("/403"))
				.userDetailsService(userDetailsService);

		return http.build();
	}

	@Bean
	public static PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}

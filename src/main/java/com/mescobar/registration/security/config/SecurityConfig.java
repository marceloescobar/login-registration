package com.mescobar.registration.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private UserDetailsService userDetailsService;
  
  @Autowired
  private AuthenticationFailureHandler authenticationFailureHandler;

  
  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
      return super.authenticationManagerBean();
  }
  
  @Override
  protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
      auth.authenticationProvider(authProvider());
  }
  
  @Bean
  public DaoAuthenticationProvider authProvider() {
      final CustomAuthenticationProvider authProvider = new CustomAuthenticationProvider();
      authProvider.setUserDetailsService(userDetailsService);
      authProvider.setPasswordEncoder(encoder());
      //authProvider.setPostAuthenticationChecks(differentLocationChecker);
      return authProvider;
  }
  
  @Bean
  public PasswordEncoder encoder() {
      return new BCryptPasswordEncoder(11);
  }
  
  @Override
  protected void configure(final HttpSecurity http) throws Exception {
      http
        .csrf().disable()
        .authorizeRequests()
        .antMatchers("/admin/**").hasRole("ADMIN")
        .antMatchers("/anonymous*").anonymous()
        .antMatchers("/login*").permitAll()
        .anyRequest().authenticated()
        .and()
        .formLogin()
        .loginPage("/login.html")
        .loginProcessingUrl("/perform_login")
        .defaultSuccessUrl("/homepage.html", true)
        .failureUrl("/login.html?error=true")
        .failureHandler(authenticationFailureHandler)
        .and()
        .logout()
        .logoutUrl("/perform_logout")
        .deleteCookies("JSESSIONID")
        .logoutSuccessHandler(logoutSuccessHandler());
  }
}

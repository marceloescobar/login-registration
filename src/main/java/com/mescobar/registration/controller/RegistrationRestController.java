package com.mescobar.registration.controller;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import com.mescobar.registration.dto.GenericResponse;
import com.mescobar.registration.dto.PasswordDTO;
import com.mescobar.registration.dto.UserDTO;
import com.mescobar.registration.event.OnRegistrationCompleteEvent;
import com.mescobar.registration.persistence.model.User;
import com.mescobar.registration.service.CaptchaService;
import com.mescobar.registration.service.SecurityUserService;
import com.mescobar.registration.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v2")
public class RegistrationRestController {

  @Autowired
  private UserService userService;
  

  @Autowired
  private SecurityUserService securityUserService;
  
  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @Autowired
  private MessageSource messages;

  @Autowired
  private JavaMailSender mailSender;
  
  @Autowired
  private CaptchaService captchaService;


  @Autowired
  private Environment env;
  
  // Registration
  @PostMapping("/user/registration")
  public GenericResponse registerUserAccount(@Valid final UserDTO accountDto, final HttpServletRequest request) {
      log.debug("Registering user account with information: {}", accountDto);

      final User registered = userService.registerNewUserAccount(accountDto);
     // userService.addUserLocation(registered, getClientIP(request));
      eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered, request.getLocale(), getAppUrl(request)));
      
      return new GenericResponse("success");
  }
  
  // Registration
  @PostMapping("/user/registrationCaptcha")
  public GenericResponse captchaRegisterUserAccount(@Valid final UserDTO accountDto, final HttpServletRequest request) {

      final String response = request.getParameter("g-recaptcha-response");
      captchaService.processResponse(response);

      return registerNewUserHandler(accountDto, request);
  }
  
  private GenericResponse registerNewUserHandler(final UserDTO accountDto, final HttpServletRequest request) {
    log.debug("Registering user account with information: {}", accountDto);

    final User registered = userService.registerNewUserAccount(accountDto);
    eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered, request.getLocale(), getAppUrl(request)));
    return new GenericResponse("success");
}
  
  
  
  // Reset password
  @PostMapping("/user/resetPassword")
  public GenericResponse resetPassword(final HttpServletRequest request, @RequestParam("email") final String userEmail) {
      final User user = userService.findUserByEmail(userEmail);
     
      if (user != null) {
          final String token = UUID.randomUUID().toString();
          userService.createPasswordResetTokenForUser(user, token);
          mailSender.send(constructResetTokenEmail(getAppUrl(request), request.getLocale(), token, user));
      }
      return new GenericResponse(messages.getMessage("message.resetPasswordEmail", null, request.getLocale()));
  }
  
  @PostMapping("/user/savePassword")
  public GenericResponse savePassword(final Locale locale, @Valid PasswordDTO passwordDto) {

      String result = securityUserService.validatePasswordResetToken(passwordDto.getToken());

      if(result != null) {
          return new GenericResponse(messages.getMessage(
              "auth.message." + result, null, locale));
      }

      Optional<User> user = userService.getUserByPasswordResetToken(passwordDto.getToken());
      if(user.isPresent()) {
          userService.changeUserPassword(user.get(), passwordDto.getNewPassword());
          return new GenericResponse(messages.getMessage(
              "message.resetPasswordSuc", null, locale));
      } else {
          return new GenericResponse(messages.getMessage(
              "auth.message.invalid", null, locale));
      }
  }
  
  private SimpleMailMessage constructResetTokenEmail(
      String contextPath, Locale locale, String token, User user) {
        String url = contextPath + "/user/changePassword?token=" + token;
        String message = messages.getMessage("message.resetPassword", 
          null, locale);
        return constructEmail("Reset Password", message + " \r\n" + url, user);
    }

    private SimpleMailMessage constructEmail(String subject, String body, 
      User user) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(user.getEmail());
        email.setFrom(env.getProperty("support.email"));
        return email;
    }
    
    private String getAppUrl(HttpServletRequest request) {
      return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
  }
  
}

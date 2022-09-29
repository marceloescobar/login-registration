package com.mescobar.registration.controller;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import com.mescobar.registration.dto.UserDTO;
import com.mescobar.registration.event.OnRegistrationCompleteEvent;
import com.mescobar.registration.exception.UserAlreadyExistException;
import com.mescobar.registration.persistence.model.User;
import com.mescobar.registration.persistence.model.VerificationToken;
import com.mescobar.registration.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/v1")
public class RegistrationController {

  @Autowired
  private UserService userService;

  @Autowired
  private MessageSource messages;
  
  @Autowired
  private ApplicationEventPublisher eventPublisher;
  
  @Autowired
  private SecurityUserService securityUserService;


  @GetMapping("/user/registration")
  public String showRegistrationForm(final HttpServletRequest request, final Model model) {
    log.debug("Rendering registration page.");

    final UserDTO accountDto = new UserDTO();
    model.addAttribute("user", accountDto);

    return "registration";
  }

  @PostMapping("/user/registration")
  public ModelAndView registerUserAccount(@ModelAttribute("user") @Valid final UserDTO userDto,
      final HttpServletRequest request, final Errors errors) {
    log.debug("Registering user account with information: {}", userDto);

    try {
      final User registered = userService.registerNewUserAccount(userDto);

      // final String appUrl = "http://" + request.getServerName() + ":" + request.getServerPort() +
      // request.getContextPath();
      // eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered,
      // request.getLocale(), appUrl));
      
      String appUrl = request.getContextPath();
      eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered, 
        request.getLocale(), appUrl));

    } catch (final UserAlreadyExistException uaeEx) {
      ModelAndView mav = new ModelAndView("registration", "user", userDto);
      String errMessage = messages.getMessage("message.regError", null, request.getLocale());
      mav.addObject("message", errMessage);
      return mav;

    } catch (final RuntimeException ex) {
      log.warn("Unable to register user", ex);
      return new ModelAndView("emailError", "user", userDto);

    }

    return new ModelAndView("successRegister", "user", userDto);
  }
  
  @GetMapping("/registrationConfirm")
  public String confirmRegistration(final HttpServletRequest request, final Model model, @RequestParam("token") final String token) {
      final Locale locale = request.getLocale();

      final VerificationToken verificationToken = userService.getVerificationToken(token);
      
      if (verificationToken == null) {
          final String message = messages.getMessage("auth.message.invalidToken", null, locale);
          model.addAttribute("message", message);
          return "redirect:/badUser.html?lang=" + locale.getLanguage();
      }

      final User user = verificationToken.getUser();
      final Calendar cal = Calendar.getInstance();
      if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
          model.addAttribute("message", messages.getMessage("auth.message.expired", null, locale));
          model.addAttribute("expired", true);
          model.addAttribute("token", token);
          return "redirect:/badUser.html?lang=" + locale.getLanguage();
      }

      user.setEnabled(true);
      userService.saveRegisteredUser(user);
      
      model.addAttribute("message", messages.getMessage("message.accountVerified", null, locale));
      return "redirect:/login.html?lang=" + locale.getLanguage();
  }
  
  @GetMapping("/user/resendRegistrationToken")
  public String resendRegistrationToken(final HttpServletRequest request, final Model model, @RequestParam("token") final String existingToken) {
      final Locale locale = request.getLocale();
      final VerificationToken newToken = userService.generateNewVerificationToken(existingToken);
      final User user = userService.getUser(newToken.getToken());
      try {
          final String appUrl = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
          final SimpleMailMessage email = constructResetVerificationTokenEmail(appUrl, request.getLocale(), newToken, user);
          mailSender.send(email);
      } catch (final MailAuthenticationException e) {
          LOGGER.debug("MailAuthenticationException", e);
          return "redirect:/emailError.html?lang=" + locale.getLanguage();
      } catch (final Exception e) {
          LOGGER.debug(e.getLocalizedMessage(), e);
          model.addAttribute("message", e.getLocalizedMessage());
          return "redirect:/login.html?lang=" + locale.getLanguage();
      }
      model.addAttribute("message", messages.getMessage("message.resendToken", null, locale));
      return "redirect:/login.html?lang=" + locale.getLanguage();
  }
  
  
  @GetMapping("/user/changePassword")
  public ModelAndView showChangePasswordPage(final ModelMap model, @RequestParam("token") final String token) {
      final String result = securityUserService.validatePasswordResetToken(token);

      if(result != null) {
          String messageKey = "auth.message." + result;
          model.addAttribute("messageKey", messageKey);
          return new ModelAndView("redirect:/login", model);
      } else {
          model.addAttribute("token", token);
          return new ModelAndView("redirect:/updatePassword");
      }
  }
  
  private SimpleMailMessage constructResetVerificationTokenEmail(final String contextPath, final Locale locale, final VerificationToken newToken, final User user) {
    final String confirmationUrl = contextPath + "/old/registrationConfirm.html?token=" + newToken.getToken();
    final String message = messages.getMessage("message.resendToken", null, locale);
    final SimpleMailMessage email = new SimpleMailMessage();
    email.setSubject("Resend Registration Token");
    email.setText(message + " \r\n" + confirmationUrl);
    email.setTo(user.getEmail());
    email.setFrom(env.getProperty("support.email"));
    return email;
}



}

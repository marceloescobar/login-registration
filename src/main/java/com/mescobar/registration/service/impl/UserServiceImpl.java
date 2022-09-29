package com.mescobar.registration.service.impl;

import java.util.Arrays;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.mescobar.registration.dto.UserDTO;
import com.mescobar.registration.exception.UserAlreadyExistException;
import com.mescobar.registration.persistence.model.PasswordResetToken;
import com.mescobar.registration.persistence.model.User;
import com.mescobar.registration.persistence.model.VerificationToken;
import com.mescobar.registration.persistence.repository.PasswordResetTokenRepository;
import com.mescobar.registration.persistence.repository.UserRepository;
import com.mescobar.registration.persistence.repository.VerificationTokenRepository;
import com.mescobar.registration.service.UserService;

@Service
@Transactional
public class UserServiceImpl implements UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private VerificationTokenRepository tokenRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;


  @Autowired
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @Override
  public User registerNewUserAccount(final UserDTO accountDto) {

    if (emailExists(accountDto.getEmail())) {
      throw new UserAlreadyExistException(
          "Já existe uma conta registrada com o email: " + accountDto.getEmail());
    }

    final User user = new User();

    user.setFirstName(accountDto.getFirstName());
    user.setLastName(accountDto.getLastName());

    user.setPassword(passwordEncoder.encode(accountDto.getPassword()));

    user.setEmail(accountDto.getEmail());
    user.setRoles(Arrays.asList("ROLE_USER"));

    return userRepository.save(user);
  }

  private boolean emailExists(final String email) {
    return userRepository.findByEmail(email) != null;
  }

  @Override
  public User getUser(String verificationToken) {
    User user = tokenRepository.findByToken(verificationToken).getUser();
    return user;
  }

  @Override
  public VerificationToken getVerificationToken(String VerificationToken) {
    return tokenRepository.findByToken(VerificationToken);
  }

  @Override
  public void saveRegisteredUser(User user) {
    userRepository.save(user);
  }

  @Override
  public void createVerificationToken(User user, String token) {
    VerificationToken myToken = new VerificationToken(token, user);
    tokenRepository.save(myToken);
  }


  @Override
  public void createPasswordResetTokenForUser(final User user, final String token) {
    final PasswordResetToken myToken = new PasswordResetToken(token, user);
    passwordResetTokenRepository.save(myToken);
  }

  @Override
  public User findUserByEmail(final String email) {
    return userRepository.findByEmail(email);
  }

  public void changeUserPassword(User user, String password) {
    user.setPassword(passwordEncoder.encode(password));
    userRepository.save(user);
  }
  
  @Override
  public void createVerificationTokenForUser(final User user, final String token) {
      final VerificationToken myToken = new VerificationToken(token, user);
      tokenRepository.save(myToken);
  }

  @Override
  public Optional<User> getUserByPasswordResetToken(final String token) {
      return Optional.ofNullable(passwordResetTokenRepository.findByToken(token) .getUser());
  }

}

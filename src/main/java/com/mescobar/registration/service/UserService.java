package com.mescobar.registration.service;

import java.util.Optional;
import com.mescobar.registration.dto.UserDTO;
import com.mescobar.registration.exception.UserAlreadyExistException;
import com.mescobar.registration.persistence.model.User;
import com.mescobar.registration.persistence.model.VerificationToken;

public interface UserService {

  User registerNewUserAccount(UserDTO accountDto) throws UserAlreadyExistException;

  User getUser(String verificationToken);

  void saveRegisteredUser(User user);

  void createVerificationToken(User user, String token);

  VerificationToken getVerificationToken(String VerificationToken);

  void createPasswordResetTokenForUser(User user, String token);

  User findUserByEmail(final String email);

  void changeUserPassword(User user, String password);

  void createVerificationTokenForUser(final User user, final String token);


  Optional<User> getUserByPasswordResetToken(final String token);

}

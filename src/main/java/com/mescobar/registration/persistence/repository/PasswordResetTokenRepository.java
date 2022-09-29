package com.mescobar.registration.persistence.repository;

import java.util.Date;
import java.util.stream.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import com.mescobar.registration.persistence.model.PasswordResetToken;
import com.mescobar.registration.persistence.model.User;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

  PasswordResetToken findByToken(String token);

  PasswordResetToken findByUser(User user);

  Stream<PasswordResetToken> findAllByExpiryDateLessThan(Date now);

  void deleteByExpiryDateLessThan(Date now);

 
}

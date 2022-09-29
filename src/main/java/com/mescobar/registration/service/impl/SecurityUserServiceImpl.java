package com.mescobar.registration.service.impl;

import javax.transaction.Transactional;
import com.mescobar.registration.persistence.model.PasswordResetToken;
import com.mescobar.registration.service.SecurityUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class SecurityUserServiceImpl implements SecurityUserService {

  @Autowired
  private PasswordResetTokenRepository passwordTokenRepository;

  @Override
  public String validatePasswordResetToken(String token) {
      final PasswordResetToken passToken = passwordTokenRepository.findByToken(token);

      return !isTokenFound(passToken) ? "invalidToken"
              : isTokenExpired(passToken) ? "expired"
              : null;
  }

  private boolean isTokenFound(PasswordResetToken passToken) {
      return passToken != null;
  }

  private boolean isTokenExpired(PasswordResetToken passToken) {
      final Calendar cal = Calendar.getInstance();
      return passToken.getExpiryDate().before(cal.getTime());
  }
}

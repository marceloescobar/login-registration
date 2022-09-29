package com.mescobar.registration.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mescobar.registration.persistence.model.User;
import com.mescobar.registration.persistence.model.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long>{

  VerificationToken findByToken(String token);

  VerificationToken findByUser(User user);
  
}

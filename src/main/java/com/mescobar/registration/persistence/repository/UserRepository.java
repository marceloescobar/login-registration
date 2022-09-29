package com.mescobar.registration.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mescobar.registration.persistence.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

  User findByEmail(String email);

  @Override
  void delete(User user);

}

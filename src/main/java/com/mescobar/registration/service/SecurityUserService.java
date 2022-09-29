package com.mescobar.registration.service;

public interface SecurityUserService {

  String validatePasswordResetToken(String token);

}

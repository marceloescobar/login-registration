package com.mescobar.registration.service;

import com.mescobar.registration.exception.ReCaptchaInvalidException;

public interface CaptchaService {

  default void processResponse(final String response) throws ReCaptchaInvalidException {}

  default void processResponse(final String response, String action)
      throws ReCaptchaInvalidException {}

  String getReCaptchaSite();

  String getReCaptchaSecret();
}

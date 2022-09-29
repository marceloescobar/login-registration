package com.mescobar.registration.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.mescobar.registration.validation.PasswordMatches;
import com.mescobar.registration.validation.ValidEmail;
import lombok.Data;

@Data
@PasswordMatches
public class UserDTO {

  @NotNull
  @NotEmpty
  private String firstName;

  @NotNull
  @NotEmpty
  private String lastName;

  @NotNull
  @NotEmpty
  private String password;

  @NotNull
  private String matchingPassword;

  @ValidEmail
  private String email;
  
}

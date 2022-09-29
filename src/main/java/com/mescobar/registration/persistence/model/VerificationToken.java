package com.mescobar.registration.persistence.model;

import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity
public class VerificationToken {

  private static final int EXPIRATION = 60 * 24;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String token;

  @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
  @JoinColumn(nullable = false, name = "user_id", foreignKey = @ForeignKey(name = "FK_VERIFY_USER"))
  private User user;

  private LocalDateTime expiryDate;
  
  public VerificationToken(final String token) {
    super();

    this.token = token;
    this.expiryDate = calculateExpiryDate(EXPIRATION);
}

public VerificationToken(final String token, final User user) {
    super();

    this.token = token;
    this.user = user;
    this.expiryDate = calculateExpiryDate(EXPIRATION);
}

private LocalDateTime calculateExpiryDate(final int expiryTimeInMinutes) {
  return LocalDateTime.now().plusMinutes(expiryTimeInMinutes);
  /*
   * final Calendar cal = Calendar.getInstance(); cal.setTimeInMillis(new Date().getTime());
   * cal.add(Calendar.MINUTE, expiryTimeInMinutes); return new Date(cal.getTime().getTime());
   */
}
}

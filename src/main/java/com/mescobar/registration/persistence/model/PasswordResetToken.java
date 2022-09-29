package com.mescobar.registration.persistence.model;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import lombok.Data;

@Data
@Entity
public class PasswordResetToken {

  private static final int EXPIRATION = 60 * 24;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String token;

  @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
  @JoinColumn(nullable = false, name = "user_id")
  private User user;

  private LocalDateTime expiryDate;

  public PasswordResetToken() {
    super();
  }

  public PasswordResetToken(final String token) {
    super();

    this.token = token;
    this.expiryDate = calculateExpiryDate(EXPIRATION);
  }

  public PasswordResetToken(final String token, final User user) {
    super();

    this.token = token;
    this.user = user;
    this.expiryDate = calculateExpiryDate(EXPIRATION);
  }

  private LocalDateTime calculateExpiryDate(final int expiryTimeInMinutes) {
    /*
     * final Calendar cal = Calendar.getInstance(); cal.setTimeInMillis(new Date().getTime());
     * cal.add(Calendar.MINUTE, expiryTimeInMinutes); return new Date(cal.getTime().getTime());
     */
    LocalDateTime ldt = LocalDateTime.now();
    ldt.plusMinutes(expiryTimeInMinutes);

    return ldt;
  }
}

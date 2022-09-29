package com.mescobar.registration.security;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.mescobar.registration.persistence.model.User;
import com.mescobar.registration.persistence.repository.UserRepository;

@Service("userDetailsService")
@Transactional
public class MyUserDetailsService implements UserDetailsService {

  @Autowired
  private UserRepository userRepository;
  
  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
  
        User user = userRepository.findByEmail(email);
        if (user == null) 
            throw new UsernameNotFoundException("No user found with username: " + email);
        
        boolean enabled = true;
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;
        
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(), 
            user.getPassword().toLowerCase(), 
            user.isEnabled(), 
            accountNonExpired, 
            credentialsNonExpired, 
            accountNonLocked, 
            getAuthorities(user.getRole()));
    }
  
  private static List<GrantedAuthority> getAuthorities (List<String> roles) {
    List<GrantedAuthority> authorities = new ArrayList<>();
    for (String role : roles) {
        authorities.add(new SimpleGrantedAuthority(role));
    }
    return authorities;
}
  

}

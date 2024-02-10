package team606.stockStat.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import team606.stockStat.accounting.dto.exceptions.UserExistsException;

import java.util.HashSet;
import java.util.Set;

@Service
public class SecurityServiceImpl implements SecurityService{
    private final AuthenticationManager authenticationManager;
    final UserRepository userRepository;
    private final UserDetailsService userDetailsService;

    public SecurityServiceImpl(AuthenticationManager authenticationManager, UserDetailsService userDetailsService, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @Override
    public boolean login(String username, String password) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());

        authenticationManager.authenticate(usernamePasswordAuthenticationToken);

        if (usernamePasswordAuthenticationToken.isAuthenticated()) {
            SecurityContextHolder.getContext()
                    .setAuthentication(usernamePasswordAuthenticationToken);

            return true;
        }

        return false;
    }
    public User createUser(UserDto userDto){
        Set<GrantedAuthority> set = new HashSet<>();
        Role role = new Role();
        role.setName("ROLE_ADMIN");
        UserRole userRole = new UserRole(role);
        set.add((GrantedAuthority) userRole);
        User user = new User(null,userDto.getUsername(), userDto.getPassword(), set);
        User userByUsername = userRepository.findUserByUsername(userDto.getUsername());
        if (userByUsername!=null){
            throw new UserExistsException("User with this name already exists");
        }
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword("{bcrypt}"+encode);
        User userSaved = userRepository.save(user);
        return userSaved;
    }
}

package team606.stockStat.accounting.service;

import org.mindrot.jbcrypt.BCrypt;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import team606.stockStat.accounting.dao.UserAccountRepository;
import team606.stockStat.accounting.dto.PasswordDto;
import team606.stockStat.accounting.dto.RolesDto;
import team606.stockStat.accounting.dto.UserDto;
import team606.stockStat.accounting.dto.UserEditDto;
import team606.stockStat.accounting.dto.UserRegisterDto;
import team606.stockStat.accounting.dto.exceptions.UserExistsException;
import team606.stockStat.accounting.dto.exceptions.UserNotFoundException;
import team606.stockStat.accounting.model.UserAccount;



@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService , CommandLineRunner{
	
	final UserAccountRepository userAccountRepository;
	final ModelMapper modelMapper;
	final PasswordEncoder passwordEncoder;

	@Override
	public UserDto registerUser(UserRegisterDto userRegisterDto) {
		if (userAccountRepository.existsById(userRegisterDto.getLogin())) {
	        throw new UserExistsException("User already exists");
	    }
	    UserAccount userAccount = modelMapper.map(userRegisterDto, UserAccount.class);
	  
	    userAccount.addRole("USER");
	    String password = passwordEncoder.encode(userRegisterDto.getPassword());
	    userAccount.setPassword(password);
	    userAccountRepository.save(userAccount);
	    return modelMapper.map(userAccount, UserDto.class);
	}

	@Override
	public UserDto loginUser(String login, String password) {
		UserAccount userAccount = userAccountRepository.findById(login)
                .orElseThrow(UserNotFoundException::new);
        
        if (passwordEncoder.matches(password, userAccount.getPassword())) {
            return modelMapper.map(userAccount, UserDto.class);
        } else {
            // Password does not match
            throw new UserNotFoundException();
        }
	}

	@Override
	public UserDto updateUser(String login, UserEditDto userEditDto) {
		UserAccount userAccount = userAccountRepository.findById(login)
                .orElseThrow(UserNotFoundException::new);
        userAccount.setFirstName(userEditDto.getFirstName());
        userAccount.setLastName(userEditDto.getLastName());
        userAccountRepository.save(userAccount);
        return modelMapper.map(userAccount, UserDto.class);
	}

	@Override
	public UserDto deleteUser(String login) {
		UserAccount userAccount = userAccountRepository.findById(login).orElseThrow(UserNotFoundException::new);
		userAccountRepository.deleteById(login);
		return modelMapper.map(userAccount, UserDto.class);
		
	}

	@Override
	public RolesDto addRoleToUser(String login, String roleName) {
		 UserAccount userAccount = userAccountRepository.findById(login)
	                .orElseThrow(UserNotFoundException::new);
	        
	        if (userAccount.addRole(roleName)) {
	            userAccountRepository.save(userAccount);
	        }
	        
	        return modelMapper.map(userAccount, RolesDto.class);
	}

	@Override
	public RolesDto deleteRoleFromUser(String login, String roleName) {
		 UserAccount userAccount = userAccountRepository.findById(login)
	                .orElseThrow(UserNotFoundException::new);
	        
	        if (userAccount.removeRole(roleName)) {
	            userAccountRepository.save(userAccount);
	        }
	        
	        return modelMapper.map(userAccount, RolesDto.class);
	}

	@Override
	public void changePassword(String login, String newPassword) {
		UserAccount userAccount = userAccountRepository.findById(login)
                .orElseThrow(UserNotFoundException::new);
		String password = passwordEncoder.encode(newPassword);
        userAccount.setPassword(password);
        userAccountRepository.save(userAccount);
		
	}

	@Override
	public UserDto getUser(String login) {
		UserAccount userAccount = userAccountRepository.findById(login).orElseThrow(UserNotFoundException::new);
		return modelMapper.map(userAccount, UserDto.class);
	}

	@Override
	public RolesDto changeRolesList(String login, String role, boolean b) {
		UserAccount userAccount = userAccountRepository.findById(login)
                .orElseThrow(UserNotFoundException::new);
        if (b) {
            userAccount.addRole(role);
        } else {
            userAccount.removeRole(role);
        }
        userAccountRepository.save(userAccount);
        return modelMapper.map(userAccount, RolesDto.class);
	}

	@Override
	public void run(String... args) throws Exception {
		if(!userAccountRepository.existsById("admin")) {
			String password = BCrypt.hashpw("admin", BCrypt.gensalt());
			UserAccount userAccount = new UserAccount("admin", password, "", "");
			userAccount.addRole("USER");
			userAccount.addRole("MODERATOR");
			userAccount.addRole("ADMINISTRATOR");
			userAccountRepository.save(userAccount);
		}
		
	}

	@Override
	public void sendRecoveryPasswordLink(String email) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void recoverPassword(String token, PasswordDto passwordDto) {
		// TODO Auto-generated method stub
		
	}

	

}

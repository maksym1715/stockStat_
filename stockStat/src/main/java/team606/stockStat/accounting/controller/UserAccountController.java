package team606.stockStat.accounting.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import team606.stockStat.accounting.dto.PasswordDto;
import team606.stockStat.accounting.dto.RolesDto;
import team606.stockStat.accounting.dto.UserDto;
import team606.stockStat.accounting.dto.UserEditDto;
import team606.stockStat.accounting.dto.UserRegisterDto;
import team606.stockStat.accounting.dto.exceptions.UnauthorizedException;
import team606.stockStat.accounting.service.UserService;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class UserAccountController {
	
	final UserService userService;
	
	@PostMapping("/register")
	public UserDto registerUser(@RequestBody UserRegisterDto userRegisterDto) {
		
		return userService.registerUser(userRegisterDto);
	}
	
	@PostMapping("/login")
	public UserDto loginUser(Principal principal) {
		return userService.getUser(principal.getName());
	}
	
	@GetMapping("/user/{login}")
	public UserDto getUser(@PathVariable String login) {
		return userService.getUser(login);
	}
	
	@DeleteMapping("/user/{login}")
	public UserDto deleteUser( @PathVariable String login) {
		
		return userService.deleteUser(login);
	}
	
	@PutMapping("/user/{login}")
	public UserDto updateUser(@PathVariable String login, @RequestBody UserEditDto userEditDto) {
		
		return userService.updateUser(login, userEditDto);
	}
	
	@PutMapping("/user/{user}/role/{role}")
	public RolesDto addRoleToUser(@PathVariable String login,@PathVariable String role) {
		
		return userService.changeRolesList(login, role, true);
	}
	
	@DeleteMapping("/user/{user}/role/{role}")
	public RolesDto deleteRoleFromUser(@PathVariable String login,@PathVariable String role) {
		
		return userService.changeRolesList(login, role, false);
	}
	
	@PutMapping("/password")
	public void changePassword(Principal principal, @RequestHeader("X-Password") String newPassword) {
		if (principal == null) {
	        throw new UnauthorizedException("User is not authenticated");
	    }
		 String login = principal.getName();
		    userService.changePassword(login, newPassword);
			
		}
	
	@GetMapping("/recovery/{email}")
    public ResponseEntity<Void> sendRecoveryPasswordLink(@PathVariable String email) {
        userService.sendRecoveryPasswordLink(email);
        return ResponseEntity.ok().build();
    }
	
	@PostMapping("/recovery/{token}")
    public ResponseEntity<Void> recoverPassword(@PathVariable String token, @RequestBody PasswordDto passwordDto) {
        userService.recoverPassword(token, passwordDto);
        return ResponseEntity.ok().build();
    }
}

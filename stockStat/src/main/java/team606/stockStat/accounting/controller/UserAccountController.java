package team606.stockStat.accounting.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import team606.stockStat.security.SecurityServiceImpl;
import team606.stockStat.security.User;
import team606.stockStat.security.UserDto;

import javax.annotation.security.RolesAllowed;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
@AllArgsConstructor
public class UserAccountController {

    @RolesAllowed("ROLE_ADMIN")
    @GetMapping("/admin")
    public String admin() {
        return "Hello Admin!";
    }

    @RolesAllowed({ "ROLE_ADMIN", "ROLE_USER" })
    @GetMapping("/user")
    public String user() {
        return "Hello User!";
    }

    @Autowired
    SecurityServiceImpl userService;

	@PostMapping("/register")
	public User registerUser(@RequestBody UserDto userDto) {
		return userService.createUser(userDto);
	}

	@PostMapping("/login")
	public boolean loginUser(@RequestBody UserDto userDto) {
		return userService.login(userDto.getUsername(),userDto.getPassword());
	}
//
//	@GetMapping("/user/{login}")
//	public UserDto getUser(@PathVariable String login) {
//		return userService.getUser(login);
//	}
//
//	@DeleteMapping("/user/{login}")
//	public UserDto deleteUser( @PathVariable String login) {
//
//		return userService.deleteUser(login);
//	}
//
//	@PutMapping("/user/{login}")
//	public UserDto updateUser(@PathVariable String login, @RequestBody UserEditDto userEditDto) {
//
//		return userService.updateUser(login, userEditDto);
//	}
//
//	@PutMapping("/user/{user}/role/{role}")
//	public RolesDto addRoleToUser(@PathVariable String login,@PathVariable String role) {
//
//		return userService.changeRolesList(login, role, true);
//	}
//
//	@DeleteMapping("/user/{user}/role/{role}")
//	public RolesDto deleteRoleFromUser(@PathVariable String login,@PathVariable String role) {
//
//		return userService.changeRolesList(login, role, false);
//	}
//
//	@PutMapping("/password")
//	public void changePassword(Principal principal, @RequestHeader("X-Password") String newPassword) {
//		if (principal == null) {
//	        throw new UnauthorizedException("User is not authenticated");
//	    }
//		 String login = principal.getName();
//		    userService.changePassword(login, newPassword);
//
//		}
//
//	@GetMapping("/recovery/{email}")
//    public ResponseEntity<Void> sendRecoveryPasswordLink(@PathVariable String email) {
//        userService.sendRecoveryPasswordLink(email);
//        return ResponseEntity.ok().build();
//    }
//
//	@PostMapping("/recovery/{token}")
//    public ResponseEntity<Void> recoverPassword(@PathVariable String token, @RequestBody PasswordDto passwordDto) {
//        userService.recoverPassword(token, passwordDto);
//        return ResponseEntity.ok().build();
//    }
}

package team606.stockStat.accounting.service;

import team606.stockStat.accounting.dto.PasswordDto;
import team606.stockStat.accounting.dto.RolesDto;
import team606.stockStat.accounting.dto.UserDto;
import team606.stockStat.accounting.dto.UserEditDto;
import team606.stockStat.accounting.dto.UserRegisterDto;

public interface UserService {
	UserDto registerUser(UserRegisterDto userRegisterDto);

	UserDto loginUser(String login, String password);

	UserDto updateUser(String login, UserEditDto userEditDto);
	
	UserDto getUser(String login);

	UserDto deleteUser(String login);

	RolesDto addRoleToUser(String login, String roleName);

	RolesDto deleteRoleFromUser(String login, String roleName);

	void changePassword(String login, String newPassword);

	RolesDto changeRolesList(String login, String role, boolean b);
	
	void sendRecoveryPasswordLink(String email);
	
	void recoverPassword(String token, PasswordDto passwordDto);

}

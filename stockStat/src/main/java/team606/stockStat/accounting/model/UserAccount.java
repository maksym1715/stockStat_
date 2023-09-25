package team606.stockStat.accounting.model;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;

import lombok.Setter;
@Getter
@Document(collection = "users")
public class UserAccount {
	@Id
	String login;
	@Setter
	 private String password;
	@Setter
	 private String firstName;
	@Setter
	 private String lastName;
	Set<String> roles;
	
	public UserAccount() {
        this.roles = new HashSet<>();
    }
	
	
	public UserAccount(String login, String password, String firstName, String lastName) {
		this.login = login;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		roles = new HashSet<>();
	}
	
	public boolean addRole(String role) {
		return roles.add(role);
	}
	
	public boolean removeRole(String role) {
		return roles.remove(role);
	}
}

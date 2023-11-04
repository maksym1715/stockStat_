package team606.stockStat.security;

import java.util.Objects;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;

import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Document(collection = "users")
public class User implements UserDetails {


	private @MongoId ObjectId id;
	private String username;
	private String password;
	private Set<UserRole> grantedAuthorities;

	public User(ObjectId id,String username, String password, Set<GrantedAuthority> grantedAuthorities) {
		this.id=id;
		this.username = username;
		this.password = password;
		this.grantedAuthorities = (Set)grantedAuthorities;
	}

	public ObjectId getId() {
		return id;
	}

	@Override
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setGrantedAuthorities(Set<UserRole> grantedAuthorities) {
		this.grantedAuthorities = grantedAuthorities;
	}

	@Override
	public Set<UserRole> getAuthorities() {
		return this.grantedAuthorities;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {

		return true;
	}

	@Override
	public boolean isEnabled() {

		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		User user = (User) o;
		return Objects.equals(username, user.username);
	}

	@Override
	public int hashCode() {
		return Objects.hash(username);
	}
}

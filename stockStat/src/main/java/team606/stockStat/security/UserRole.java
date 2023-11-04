package team606.stockStat.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@Getter
@AllArgsConstructor
public class UserRole implements GrantedAuthority {

    private Role role;

    @Override
    public String getAuthority() {
        return role.getName();
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
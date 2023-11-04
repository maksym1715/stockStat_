package team606.stockStat.accounting.model;

import org.springframework.security.core.GrantedAuthority;

public class UserRole implements GrantedAuthority {
    @Override
    public String getAuthority() {
        return null;
    }
}

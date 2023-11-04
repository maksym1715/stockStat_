package team606.stockStat.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@Getter
public class UserDto {
    private String username;
    private String password;

}

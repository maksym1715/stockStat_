package telran.java47.accounting.dto.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserExistsException extends RuntimeException {

	private static final long serialVersionUID = 8777376761193864981L;
	public UserExistsException(String message) {
        super(message);
    }
}

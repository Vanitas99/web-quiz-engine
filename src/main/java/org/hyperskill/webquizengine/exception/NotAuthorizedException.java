package org.hyperskill.webquizengine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED, reason = "You are not authorized to do this action")
public class NotAuthorizedException extends RuntimeException {
    public NotAuthorizedException(String msg) {
        super(msg);
    }
}

package org.hyperskill.webquizengine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "There was a forbidden expression in the input form")
public class IllegalInputExpressionException extends RuntimeException {
    public IllegalInputExpressionException(String msg) {
        super(msg);
    }
}

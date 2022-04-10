package org.hyperskill.webquizengine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Input contains either illegal Maxima or Lisp function calls")
public class IllegalFunctionException extends RuntimeException{
}

package com.camara.processos_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN) // Esta anotação já define o status de erro para 403
public class AuthorizationException extends RuntimeException {
  public AuthorizationException(String message) {
    super(message);
  }
}
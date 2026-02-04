package com.acl.project.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<?> handleApiException(ApiException ex) {
    log.error("ApiException: ", ex);
    return ResponseEntity.status(ex.getErrorObject().getHttpStatus())
      .body(ex.getErrorObject().getErrorMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleApiException(Exception ex) {
    log.error("Exception: ", ex);
    return ResponseEntity.internalServerError()
      .body(ex.getMessage());
  }
}

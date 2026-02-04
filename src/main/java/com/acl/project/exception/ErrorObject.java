package com.acl.project.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Data
@Builder
public class ErrorObject {

  private HttpStatus httpStatus;
  private String errorMessage;
}

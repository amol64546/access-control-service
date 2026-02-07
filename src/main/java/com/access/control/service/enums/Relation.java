package com.access.control.service.enums;

import io.swagger.v3.oas.annotations.Hidden;

public enum Relation {

  EDITOR, VIEWER,

  @Hidden
  MEMBER,
  @Hidden
  CHILD,
  @Hidden
  PARENT,
  @Hidden
  OWNER
}

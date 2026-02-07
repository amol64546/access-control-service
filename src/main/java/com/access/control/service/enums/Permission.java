package com.access.control.service.enums;

import io.swagger.v3.oas.annotations.Hidden;

public enum Permission {

 READ,
 WRITE,

 @Hidden
 DELETE,
 @Hidden
 GRANT,
 @Hidden
 REVOKE

}

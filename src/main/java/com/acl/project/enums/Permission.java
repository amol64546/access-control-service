package com.acl.project.enums;

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

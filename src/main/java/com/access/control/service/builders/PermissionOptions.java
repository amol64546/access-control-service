package com.access.control.service.builders;

import com.access.control.service.enums.Permission;
import com.access.control.service.enums.Resource;
import com.access.control.service.enums.Subject;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PermissionOptions {
  private final Resource resource;
  private final String resourceId;
  private final Permission permission;
  private final Subject subject;
  private final String subjectId;
  private final String password;  // Optional: for caveat context
}
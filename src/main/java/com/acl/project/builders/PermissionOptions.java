package com.acl.project.builders;

import com.acl.project.enums.Permission;
import com.acl.project.enums.Resource;
import com.acl.project.enums.Subject;
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
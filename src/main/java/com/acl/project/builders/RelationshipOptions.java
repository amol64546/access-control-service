package com.acl.project.builders;

import com.acl.project.enums.Relation;
import com.acl.project.enums.Resource;
import com.acl.project.enums.Subject;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RelationshipOptions {
  private final Resource resource;
  private final String resourceId;
  private final Relation relation;
  private final Subject subject;
  private final String subjectId;
  private final Relation subRelation;    // Optional: for group membership
  private final String password;         // Optional: for caveat
  private final Integer daysFromNow;  // Optional: for expiration

}
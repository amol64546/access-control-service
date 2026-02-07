package com.access.control.service.builders;

import com.access.control.service.enums.Relation;
import com.access.control.service.enums.Resource;
import com.access.control.service.enums.Subject;
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
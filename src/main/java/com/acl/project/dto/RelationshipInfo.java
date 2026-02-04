package com.acl.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipInfo {
  private String resourceType;
  private String resourceId;
  private String relation;
  private String toResourceType;
  private String toResourceId;
}
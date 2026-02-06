package com.acl.project.dto;

import com.acl.project.enums.AccessType;
import com.acl.project.enums.Relation;
import com.acl.project.enums.Resource;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HierarchyRelation {

  private AccessType accessType;
  private Resource resource;
  private String resourceId;
  private Integer level;
  private Relation relation;
}
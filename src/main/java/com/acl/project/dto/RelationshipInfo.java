package com.acl.project.dto;

import com.acl.project.enums.Relation;
import com.acl.project.enums.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipInfo {

  private Resource resource;
  private String resourceId;
  private Relation relation;
  private Resource toResource;
  private String toResourceId;
}
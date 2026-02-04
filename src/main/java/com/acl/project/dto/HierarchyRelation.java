package com.acl.project.dto;

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
  private String relationType; // "direct", "indirect"
  private String resourceType;
  private String resourceId;
  private Integer level; // 1 for direct, 2+ for indirect
  private String relation; // "parent", "child"
}
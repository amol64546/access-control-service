package com.access.control.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HierarchySummary {

  private Integer totalRelations;
  private Integer directParents;
  private Integer directChildren;
  private Integer indirectParents;
  private Integer indirectChildren;
  private Integer maxParentLevel;
  private Integer maxChildLevel;
}
package com.access.control.service.dto;

import com.access.control.service.enums.Relation;
import com.access.control.service.enums.Resource;
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
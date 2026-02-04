package com.acl.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HierarchyResponse {
    private String resourceType;
    private String resourceId;
    private List<HierarchyRelation> relations;
    private HierarchySummary summary;
}
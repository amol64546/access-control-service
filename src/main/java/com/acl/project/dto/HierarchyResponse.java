package com.acl.project.dto;

import com.acl.project.enums.Resource;
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

    private Resource resource;
    private String resourceId;
    private List<HierarchyRelation> relations;
    private HierarchySummary summary;
}
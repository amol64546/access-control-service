package com.acl.project.dto;
import com.acl.project.enums.Relation;
import com.acl.project.enums.Resource;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupAccessRequest {

    private Resource resource;
    private String resourceId;
    private Relation relation;
    private String password;
}
package com.acl.project.dto;
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
    private String resourceType;
    private String resourceId;
    private String groupId;
    private String relation; // "editor", "viewer", "writer", "reader", "member"
    private String requesterId; // Who is making the request
}
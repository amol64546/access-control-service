package com.acl.project.services;

import com.acl.project.builders.PermissionOptions;
import com.acl.project.builders.RelationshipOptions;
import com.acl.project.dto.GroupAccessRequest;
import com.acl.project.enums.Permission;
import com.acl.project.enums.Relation;
import com.acl.project.enums.Resource;
import com.acl.project.enums.Subject;
import com.acl.project.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.acl.project.utils.constants.TENANT_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {

  private final AuthorizationService authorizationService;

  /**
   * Create a new group
   */
  public void createGroup(String groupId, HttpServletRequest httpServletRequest) {
    String tenantId = httpServletRequest.getHeader(TENANT_ID);

    // Set the creator as owner
    authorizationService.writeRelationship(
      RelationshipOptions.builder()
        .resource(Resource.GROUP).resourceId(groupId)
        .subject(Subject.TENANT).subjectId(tenantId)
        .relation(Relation.OWNER).build()
    );

    log.info("Created group {} with owner {}", groupId, tenantId);
  }


  /**
   * Add a member to a group (only owner or admin can do this)
   */
  public void addGroupMember(String groupId, String memberId, HttpServletRequest httpServletRequest) {
    log.info("Add member to group {}: {} by {}", groupId, memberId, httpServletRequest);
    String tenantId = httpServletRequest.getHeader(TENANT_ID);

    // Check if requester has manage_members permission
    if (!authorizationService.checkPermission(PermissionOptions.builder()
      .resource(Resource.GROUP).resourceId(groupId)
      .subject(Subject.TENANT).subjectId(tenantId)
      .permission(Permission.WRITE).build())) {
      throw new ApiException(HttpStatus.FORBIDDEN,
        "Only group owner or admin can add members");
    }
    authorizationService.writeRelationship(
      RelationshipOptions.builder()
        .resource(Resource.GROUP).resourceId(groupId)
        .subject(Subject.TENANT).subjectId(memberId)
        .relation(Relation.MEMBER).build()
    );
    log.info("Added member {} to group {} by {}", memberId,
      groupId, tenantId);
  }

  /**
   * Remove a member from a group (only owner or admin can do this)
   */
  public void removeGroupMember(String groupId, String memberId, HttpServletRequest httpServletRequest) {
    log.info("Remove member {} from group {} by {}", memberId, groupId, httpServletRequest);
    String tenantId = httpServletRequest.getHeader(TENANT_ID);

    // Check if requester has manage_members permission
    if (!authorizationService.checkPermission(PermissionOptions.builder()
      .resource(Resource.GROUP).resourceId(groupId)
      .subject(Subject.TENANT).subjectId(tenantId)
      .permission(Permission.WRITE).build())) {
      throw new ApiException(HttpStatus.FORBIDDEN,
        "Only group owner or admin can remove members");
    }

    // Prevent removing owner
    if (!authorizationService.checkPermission(PermissionOptions.builder()
      .resource(Resource.GROUP).resourceId(groupId)
      .subject(Subject.TENANT).subjectId(tenantId)
      .permission(Permission.DELETE).build())) {
      throw new ApiException(HttpStatus.BAD_REQUEST,
        "Cannot remove group owner");
    }

    authorizationService.deleteRelationship(
      Resource.GROUP, groupId,
      Relation.MEMBER, Subject.TENANT, memberId
    );
    log.info("Removed member {} from group {} by {}", memberId,
      groupId, tenantId);
  }

  /**
   * Grant group access to a resource (only owner or admin can do this)
   */
  public void grantGroupAccess(String groupId, GroupAccessRequest request, HttpServletRequest httpServletRequest) {
    log.info("Grant group access: {}", request);
    String tenantId = httpServletRequest.getHeader(TENANT_ID);

    // Check if requester has manage_members permission on the group
    if (!authorizationService.checkPermission(PermissionOptions.builder()
      .resource(Resource.GROUP).resourceId(groupId)
      .subject(Subject.TENANT).subjectId(tenantId)
      .permission(Permission.WRITE).build())) {
      throw new ApiException(HttpStatus.FORBIDDEN,
        "Only group owner or admin can grant group access");
    }

    // Also check if requester has grant permission on the resource
    if (!authorizationService.checkPermission(PermissionOptions.builder()
      .resource(request.getResource()).resourceId(request.getResourceId())
      .subject(Subject.TENANT).subjectId(tenantId)
      .permission(Permission.GRANT).build())) {
      throw new ApiException(HttpStatus.FORBIDDEN,
        "You don't have permission to grant access to this resource");
    }
    authorizationService.writeRelationship(RelationshipOptions.builder()
      .resource(request.getResource()).resourceId(request.getResourceId())
      .subject(Subject.GROUP).subjectId(groupId).subRelation(Relation.MEMBER)
      .password(request.getPassword()).daysFromNow(request.getDaysFromNow())
      .relation(request.getRelation()).build()
    );

    log.info("Granted {} access to {}:{} for group {} by {}",
      request.getRelation(), request.getResource(), request.getResourceId(),
      groupId, tenantId);
  }

  /**
   * Revoke group access from a resource (only owner or admin can do this)
   */
  public void revokeGroupAccess(String groupId, GroupAccessRequest request, HttpServletRequest httpServletRequest) {
    log.info("Revoke group access: {}", request);
    String tenantId = httpServletRequest.getHeader(TENANT_ID);

    // Check if requester has manage members permission on the group
    if (!authorizationService.checkPermission(PermissionOptions.builder()
      .resource(Resource.GROUP).resourceId(groupId)
      .subject(Subject.TENANT).subjectId(tenantId)
      .permission(Permission.WRITE).build())) {
      throw new ApiException(HttpStatus.FORBIDDEN,
        "Only group owner or admin can revoke group access");
    }

    // Also check if requester has revoke permission on the resource
    if (!authorizationService.checkPermission(PermissionOptions.builder()
      .resource(request.getResource()).resourceId(request.getResourceId())
      .subject(Subject.TENANT).subjectId(tenantId)
      .permission(Permission.REVOKE).build())) {
      throw new ApiException(HttpStatus.FORBIDDEN,
        "You don't have permission to revoke access from this resource");
    }

    authorizationService.deleteRelationship(
      request.getResource(), request.getResourceId(),
      request.getRelation(), Subject.GROUP, groupId
    );
    log.info("Revoked {} access from {}:{} for group {} by {}",
      request.getRelation(), request.getResource(), request.getResourceId(),
      groupId, tenantId);
  }

  /**
   * Delete a group (only owner can do this)
   */
  public void deleteGroup(String groupId, HttpServletRequest httpServletRequest) {
    String tenantId = httpServletRequest.getHeader(TENANT_ID);

    log.info("Delete group {} by {}", groupId, tenantId);

    // Check if requester is owner
    if (!authorizationService.checkPermission(PermissionOptions.builder()
      .resource(Resource.GROUP).resourceId(groupId)
      .subject(Subject.TENANT).subjectId(tenantId)
      .permission(Permission.DELETE).build())) {
      throw new ApiException(HttpStatus.FORBIDDEN,
        "Only group owner can delete the group");
    }

    // Delete all relationships for this group
    authorizationService.deleteRelationship(Resource.GROUP, groupId);
    log.info("Deleted group {} by {}", groupId, tenantId);
  }

  public List<String> getAllMembers(String groupId, HttpServletRequest httpServletRequest) {
    String tenantId = httpServletRequest.getHeader(TENANT_ID);

    // Security gate: only owner can list members (adjust if needed)
    if (!authorizationService.checkPermission(
      PermissionOptions.builder()
        .resource(Resource.GROUP)
        .resourceId(groupId)
        .subject(Subject.TENANT)
        .subjectId(tenantId)
        .permission(Permission.READ)
        .build())) {

      throw new ApiException(HttpStatus.FORBIDDEN,
        "Only group owner can view members");
    }

    // Expand group#member

    return authorizationService.expandPermissionTree(
      Resource.GROUP,
      groupId,
      Relation.MEMBER
    );
  }


}
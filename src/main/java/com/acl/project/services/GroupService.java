package com.acl.project.services;

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

import static com.acl.project.utils.constants.*;

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
      Resource.GROUP, groupId,
      Relation.OWNER, TENANT, tenantId
    );
    log.info("Created group {} with owner {}", groupId, tenantId);
  }

  /**
   * Add admin to a group (only owner can do this)
   */
  public void addGroupAdmin(String groupId, String adminId, HttpServletRequest httpServletRequest) {
    log.info("Add admin to group {}: {} by {}", groupId, adminId, httpServletRequest);
    String tenantId = httpServletRequest.getHeader(TENANT_ID);

    // Check if requester is owner
    if (!authorizationService.checkPermission(Resource.GROUP, groupId, Permission.DELETE,
      Subject.TENANT, tenantId)) {
      throw new ApiException(HttpStatus.FORBIDDEN,
        "Only group owner can add admins");
    }

    authorizationService.writeRelationship(
      Resource.GROUP, groupId,
      Relation.ADMIN, TENANT, adminId
    );
    log.info("Added admin {} to group {} by {}", adminId,
      groupId, tenantId);
  }

  /**
   * Remove admin from a group (only owner can do this)
   */
  public void removeGroupAdmin(String groupId, String adminId, HttpServletRequest httpServletRequest) {
    log.info("Remove admin {} from group {} by {}", adminId, groupId, httpServletRequest);
    String tenantId = httpServletRequest.getHeader(TENANT_ID);

    // Check if requester is owner
    if (!authorizationService.checkPermission(Resource.GROUP, groupId, Permission.DELETE,
      Subject.TENANT, tenantId)) {
      throw new ApiException(HttpStatus.FORBIDDEN,
        "Only group owner can remove admins");
    }

    authorizationService.deleteRelationship(
      Resource.GROUP, groupId,
      Relation.ADMIN, Subject.TENANT, adminId
    );
    log.info("Removed admin {} from group {} by {}", adminId,
      groupId, tenantId);
  }

  /**
   * Add a member to a group (only owner or admin can do this)
   */
  public void addGroupMember(String groupId, String memberId, HttpServletRequest httpServletRequest) {
    log.info("Add member to group {}: {} by {}", groupId, memberId, httpServletRequest);
    String tenantId = httpServletRequest.getHeader(TENANT_ID);

    // Check if requester has manage_members permission
    if (!authorizationService.checkPermission(Resource.GROUP, groupId, Permission.MANAGE_MEMBERS,
      Subject.TENANT, tenantId)) {
      throw new ApiException(HttpStatus.FORBIDDEN,
        "Only group owner or admin can add members");
    }

    authorizationService.writeRelationship(
      Resource.GROUP, groupId,
      Relation.MEMBER, TENANT, memberId
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
    if (!authorizationService.checkPermission(Resource.GROUP, groupId, Permission.MANAGE_MEMBERS, Subject.TENANT,
      tenantId)) {
      throw new ApiException(HttpStatus.FORBIDDEN,
        "Only group owner or admin can remove members");
    }

    // Prevent removing owner
    if (authorizationService.checkPermission(Resource.GROUP, groupId, Permission.DELETE, Subject.TENANT,
      memberId)) {
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
    if (!authorizationService.checkPermission(Resource.GROUP, groupId,
      Permission.MANAGE_MEMBERS, Subject.TENANT, tenantId)) {
      throw new ApiException(HttpStatus.FORBIDDEN,
        "Only group owner or admin can grant group access");
    }

    // Also check if requester has grant permission on the resource
    if (!authorizationService.checkPermission(request.getResource(),
      request.getResourceId(), Permission.GRANT, Subject.TENANT, tenantId)) {
      throw new ApiException(HttpStatus.FORBIDDEN,
        "You don't have permission to grant access to this resource");
    }

    authorizationService.writeRelationshipWithSubRelation(
      request.getResource(), request.getResourceId(),
      request.getRelation(), Subject.GROUP, groupId, Relation.MEMBER
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

    // Check if requester has manage_members permission on the group
    if (!authorizationService.checkPermission(Resource.GROUP, groupId, Permission.MANAGE_MEMBERS,
      Subject.TENANT, tenantId)) {
      throw new ApiException(HttpStatus.FORBIDDEN,
        "Only group owner or admin can revoke group access");
    }

    // Also check if requester has revoke permission on the resource
    if (!authorizationService.checkPermission(request.getResource(), request.getResourceId(),
      Permission.REVOKE, Subject.TENANT, tenantId)) {
      throw new ApiException(HttpStatus.FORBIDDEN,
        "You don't have permission to revoke access from this resource");
    }

    authorizationService.deleteRelationshipWithSubRelation(
      request.getResource(), request.getResourceId(),
      request.getRelation(), Subject.GROUP, groupId, MEMBER
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
    if (!authorizationService.checkPermission(Resource.GROUP, groupId, Permission.DELETE, Subject.TENANT, tenantId)) {
      throw new ApiException(HttpStatus.FORBIDDEN,
        "Only group owner can delete the group");
    }

    // Delete all relationships for this group
    authorizationService.deleteRelationship(Resource.GROUP, groupId);
    log.info("Deleted group {} by {}", groupId, tenantId);
  }

  /**
   * Transfer group ownership (only current owner can do this)
   */
  public void transferOwnership(String groupId, String newOwnerId, HttpServletRequest httpServletRequest) {
    String currentOwnerId = httpServletRequest.getHeader(TENANT_ID);

    log.info("Transfer ownership of group {}: {}", groupId, currentOwnerId);

    if (newOwnerId.equals(currentOwnerId)) {
      throw new ApiException(HttpStatus.BAD_REQUEST,
        "New owner must be different from current owner");
    }

    // Check if requester is owner
    if (!authorizationService.checkPermission(Resource.GROUP, groupId, Permission.DELETE, Subject.TENANT, currentOwnerId)) {
      throw new ApiException(HttpStatus.FORBIDDEN,
        "Only group owner can transfer ownership");
    }

    if (!authorizationService.checkPermission(Resource.GROUP, groupId, Permission.MANAGE_MEMBERS, Subject.TENANT, newOwnerId)) {
      throw new ApiException(HttpStatus.FORBIDDEN,
        "Only group admin can be owner");
    }

    // Remove old owner
    authorizationService.deleteRelationship(Resource.GROUP, groupId, Relation.OWNER, Subject.TENANT, currentOwnerId);

    // Set new owner
    authorizationService.writeRelationship(Resource.GROUP, groupId, Relation.OWNER, TENANT, newOwnerId);

    log.info("Transferred ownership of group {} from {} to {}", groupId, currentOwnerId, newOwnerId);
  }
}
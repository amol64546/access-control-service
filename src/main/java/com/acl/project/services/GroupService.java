package com.acl.project.services;

import com.acl.project.exception.ApiException;
import com.acl.project.exception.ErrorObject;
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
  public void createGroup(String groupId, String ownerId) {
    // Set the creator as owner
    authorizationService.writeRelationship(
      GROUP_TYPE, groupId,
      OWNER, TENANT, ownerId
    );
    log.info("Created group {} with owner {}", groupId, ownerId);
  }

  /**
   * Add admin to a group (only owner can do this)
   */
  public void addGroupAdmin(String groupId, String adminId, String requesterId) {
    // Check if requester is owner
    if (!authorizationService.checkPermission(GROUP_TYPE, groupId, DELETE_GROUP, TENANT, requesterId)) {
      throw new ApiException(ErrorObject.builder()
        .httpStatus(HttpStatus.FORBIDDEN)
        .errorMessage("Only group owner can add admins")
        .build());
    }

    authorizationService.writeRelationship(
      GROUP_TYPE, groupId,
      ADMIN, TENANT, adminId
    );
    log.info("Added admin {} to group {} by {}", adminId, groupId, requesterId);
  }

  /**
   * Add a member to a group (only owner or admin can do this)
   */
  public void addGroupMember(String groupId, String memberId, String requesterId) {
    // Check if requester has manage_members permission
    if (!authorizationService.checkPermission(GROUP_TYPE, groupId, MANAGE_MEMBERS, TENANT, requesterId)) {
      throw new ApiException(ErrorObject.builder()
        .httpStatus(HttpStatus.FORBIDDEN)
        .errorMessage("Only group owner or admin can add members")
        .build());
    }

    authorizationService.writeRelationship(
      GROUP_TYPE, groupId,
      MEMBER, TENANT, memberId
    );
    log.info("Added member {} to group {} by {}", memberId, groupId, requesterId);
  }

  /**
   * Remove a member from a group (only owner or admin can do this)
   */
  public void removeGroupMember(String groupId, String memberId, String requesterId) {
    // Check if requester has manage_members permission
    if (!authorizationService.checkPermission(GROUP_TYPE, groupId, MANAGE_MEMBERS, TENANT, requesterId)) {
      throw new ApiException(ErrorObject.builder()
        .httpStatus(HttpStatus.FORBIDDEN)
        .errorMessage("Only group owner or admin can remove members")
        .build());
    }

    // Prevent removing owner
    if (authorizationService.checkPermission(GROUP_TYPE, groupId, DELETE_GROUP, TENANT, memberId)) {
      throw new ApiException(ErrorObject.builder()
        .httpStatus(HttpStatus.BAD_REQUEST)
        .errorMessage("Cannot remove group owner")
        .build());
    }

    authorizationService.deleteRelationship(
      GROUP_TYPE, groupId,
      MEMBER, TENANT, memberId
    );
    log.info("Removed member {} from group {} by {}", memberId, groupId, requesterId);
  }

  /**
   * Remove admin from a group (only owner can do this)
   */
  public void removeGroupAdmin(String groupId, String adminId, String requesterId) {
    // Check if requester is owner
    if (!authorizationService.checkPermission(GROUP_TYPE, groupId, DELETE_GROUP, TENANT, requesterId)) {
      throw new ApiException(ErrorObject.builder()
        .httpStatus(HttpStatus.FORBIDDEN)
        .errorMessage("Only group owner can remove admins")
        .build());
    }

    authorizationService.deleteRelationship(
      GROUP_TYPE, groupId,
      ADMIN, TENANT, adminId
    );
    log.info("Removed admin {} from group {} by {}", adminId, groupId, requesterId);
  }

  /**
   * Grant group access to a resource (only owner or admin can do this)
   */
  public void grantGroupAccess(String resourceType, String resourceId,
                               String groupId, String relation, String requesterId) {
    // Check if requester has manage_members permission on the group
    if (!authorizationService.checkPermission(GROUP_TYPE, groupId, MANAGE_MEMBERS, TENANT, requesterId)) {
      throw new ApiException(ErrorObject.builder()
        .httpStatus(HttpStatus.FORBIDDEN)
        .errorMessage("Only group owner or admin can grant group access")
        .build());
    }

    // Also check if requester has grant permission on the resource
    if (!authorizationService.checkPermission(resourceType, resourceId, GRANT, TENANT, requesterId)) {
      throw new ApiException(ErrorObject.builder()
        .httpStatus(HttpStatus.FORBIDDEN)
        .errorMessage("You don't have permission to grant access to this resource")
        .build());
    }

    authorizationService.writeRelationshipWithSubRelation(
      resourceType, resourceId,
      relation, GROUP_TYPE, groupId, MEMBER
    );
    log.info("Granted {} access to {}:{} for group {} by {}",
      relation, resourceType, resourceId, groupId, requesterId);
  }

  /**
   * Revoke group access from a resource (only owner or admin can do this)
   */
  public void revokeGroupAccess(String resourceType, String resourceId,
                                String groupId, String relation, String requesterId) {
    // Check if requester has manage_members permission on the group
    if (!authorizationService.checkPermission(GROUP_TYPE, groupId, MANAGE_MEMBERS, TENANT, requesterId)) {
      throw new ApiException(ErrorObject.builder()
        .httpStatus(HttpStatus.FORBIDDEN)
        .errorMessage("Only group owner or admin can revoke group access")
        .build());
    }

    // Also check if requester has revoke permission on the resource
    if (!authorizationService.checkPermission(resourceType, resourceId, REVOKE, TENANT, requesterId)) {
      throw new ApiException(ErrorObject.builder()
        .httpStatus(HttpStatus.FORBIDDEN)
        .errorMessage("You don't have permission to revoke access from this resource")
        .build());
    }

    authorizationService.deleteRelationshipWithSubRelation(
      resourceType, resourceId,
      relation, GROUP_TYPE, groupId, MEMBER
    );
    log.info("Revoked {} access from {}:{} for group {} by {}",
      relation, resourceType, resourceId, groupId, requesterId);
  }

  /**
   * Delete a group (only owner can do this)
   */
  public void deleteGroup(String groupId, String requesterId) {
    // Check if requester is owner
    if (!authorizationService.checkPermission(GROUP_TYPE, groupId, DELETE_GROUP, TENANT, requesterId)) {
      throw new ApiException(ErrorObject.builder()
        .httpStatus(HttpStatus.FORBIDDEN)
        .errorMessage("Only group owner can delete the group")
        .build());
    }

    // Delete all relationships for this group
    authorizationService.deleteRelationship(GROUP_TYPE, groupId);
    log.info("Deleted group {} by {}", groupId, requesterId);
  }

  /**
   * Transfer group ownership (only current owner can do this)
   */
  public void transferOwnership(String groupId, String newOwnerId, String currentOwnerId) {
    // Check if requester is owner
    if (!authorizationService.checkPermission(GROUP_TYPE, groupId, DELETE_GROUP, TENANT, currentOwnerId)) {
      throw new ApiException(ErrorObject.builder()
        .httpStatus(HttpStatus.FORBIDDEN)
        .errorMessage("Only group owner can transfer ownership")
        .build());
    }

    // Remove old owner
    authorizationService.deleteRelationship(GROUP_TYPE, groupId, OWNER, TENANT, currentOwnerId);

    // Set new owner
    authorizationService.writeRelationship(GROUP_TYPE, groupId, OWNER, TENANT, newOwnerId);

    log.info("Transferred ownership of group {} from {} to {}", groupId, currentOwnerId, newOwnerId);
  }
}
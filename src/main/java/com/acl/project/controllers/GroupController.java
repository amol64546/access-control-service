package com.acl.project.controllers;

import com.acl.project.dto.*;
import com.acl.project.services.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Slf4j
public class GroupController {

  private final GroupService groupService;

  @PostMapping
  public ResponseEntity<?> createGroup(@RequestBody CreateGroupRequest request) {
    log.info("Create Group Request: {}", request);
    groupService.createGroup(request.getGroupId(), request.getOwnerId());
    return ResponseEntity.ok("Group created successfully");
  }

  @PostMapping("/{groupId}/admins")
  public ResponseEntity<?> addAdmin(
    @PathVariable String groupId,
    @RequestBody GroupAdminRequest request) {
    log.info("Add admin to group {}: {}", groupId, request);
    groupService.addGroupAdmin(groupId, request.getAdminId(), request.getRequesterId());
    return ResponseEntity.ok("Admin added successfully");
  }

  @DeleteMapping("/{groupId}/admins/{adminId}")
  public ResponseEntity<?> removeAdmin(
    @PathVariable String groupId,
    @PathVariable String adminId,
    @RequestParam String requesterId) {
    log.info("Remove admin {} from group {} by {}", adminId, groupId, requesterId);
    groupService.removeGroupAdmin(groupId, adminId, requesterId);
    return ResponseEntity.ok("Admin removed successfully");
  }

  @PostMapping("/{groupId}/members")
  public ResponseEntity<?> addMember(
    @PathVariable String groupId,
    @RequestBody GroupMemberRequest request) {
    log.info("Add member to group {}: {}", groupId, request);
    groupService.addGroupMember(groupId, request.getMemberId(), request.getRequesterId());
    return ResponseEntity.ok("Member added successfully");
  }

  @DeleteMapping("/{groupId}/members/{memberId}")
  public ResponseEntity<?> removeMember(
    @PathVariable String groupId,
    @PathVariable String memberId,
    @RequestParam String requesterId) {
    log.info("Remove member {} from group {} by {}", memberId, groupId, requesterId);
    groupService.removeGroupMember(groupId, memberId, requesterId);
    return ResponseEntity.ok("Member removed successfully");
  }

  @PostMapping("/access/grant")
  public ResponseEntity<?> grantGroupAccess(@RequestBody GroupAccessRequest request) {
    log.info("Grant group access: {}", request);
    groupService.grantGroupAccess(
      request.getResourceType(),
      request.getResourceId(),
      request.getGroupId(),
      request.getRelation(),
      request.getRequesterId()
    );
    return ResponseEntity.ok("Group access granted successfully");
  }

  @DeleteMapping("/access/revoke")
  public ResponseEntity<?> revokeGroupAccess(@RequestBody GroupAccessRequest request) {
    log.info("Revoke group access: {}", request);
    groupService.revokeGroupAccess(
      request.getResourceType(),
      request.getResourceId(),
      request.getGroupId(),
      request.getRelation(),
      request.getRequesterId()
    );
    return ResponseEntity.ok("Group access revoked successfully");
  }

  @DeleteMapping("/{groupId}")
  public ResponseEntity<?> deleteGroup(
    @PathVariable String groupId,
    @RequestParam String requesterId) {
    log.info("Delete group {} by {}", groupId, requesterId);
    groupService.deleteGroup(groupId, requesterId);
    return ResponseEntity.ok("Group deleted successfully");
  }

  @PutMapping("/{groupId}/transfer-ownership")
  public ResponseEntity<?> transferOwnership(
    @PathVariable String groupId,
    @RequestBody TransferOwnershipRequest request) {
    log.info("Transfer ownership of group {}: {}", groupId, request);
    groupService.transferOwnership(groupId, request.getNewOwnerId(), request.getCurrentOwnerId());
    return ResponseEntity.ok("Ownership transferred successfully");
  }
}
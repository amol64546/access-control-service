package com.acl.project.controllers;

import com.acl.project.dto.CreateGroupRequest;
import com.acl.project.dto.GroupAccessRequest;
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
  public ResponseEntity<?> createGroup(
    @RequestBody CreateGroupRequest request,
    @RequestHeader String tenantId) {
    log.info("Create Group Request: {}", request);
    groupService.createGroup(request.getGroupId(), tenantId);
    return ResponseEntity.ok("Group created successfully");
  }

  @PostMapping("/{groupId}/admins/{adminId}")
  public ResponseEntity<?> addAdmin(
    @PathVariable String groupId,
    @PathVariable String adminId,
    @RequestHeader String tenantId) {
    log.info("Add admin to group {}: {} by {}", groupId, adminId, tenantId);
    groupService.addGroupAdmin(groupId, adminId, tenantId);
    return ResponseEntity.ok("Admin added successfully");
  }

  @DeleteMapping("/{groupId}/admins/{adminId}")
  public ResponseEntity<?> removeAdmin(
    @PathVariable String groupId,
    @PathVariable String adminId,
    @RequestHeader String tenantId) {
    log.info("Remove admin {} from group {} by {}", adminId, groupId, tenantId);
    groupService.removeGroupAdmin(groupId, adminId, tenantId);
    return ResponseEntity.ok("Admin removed successfully");
  }

  @PostMapping("/{groupId}/members/{memberId}")
  public ResponseEntity<?> addMember(
    @PathVariable String groupId,
    @PathVariable String memberId,
    @RequestHeader String tenantId) {
    log.info("Add member to group {}: {} by {}", groupId, memberId, tenantId);
    groupService.addGroupMember(groupId, memberId, tenantId);
    return ResponseEntity.ok("Member added successfully");
  }

  @DeleteMapping("/{groupId}/members/{memberId}")
  public ResponseEntity<?> removeMember(
    @PathVariable String groupId,
    @PathVariable String memberId,
    @RequestHeader String tenantId) {
    log.info("Remove member {} from group {} by {}", memberId, groupId, tenantId);
    groupService.removeGroupMember(groupId, memberId, tenantId);
    return ResponseEntity.ok("Member removed successfully");
  }

  @PostMapping("/{groupId}/access/grant")
  public ResponseEntity<?> grantGroupAccess(
    @PathVariable String groupId,
    @RequestBody GroupAccessRequest request,
    @RequestHeader String tenantId) {
    log.info("Grant group access: {}", request);
    groupService.grantGroupAccess(groupId, request, tenantId);
    return ResponseEntity.ok("Group access granted successfully");
  }

  @DeleteMapping("/{groupId}/access/revoke")
  public ResponseEntity<?> revokeGroupAccess(
    @PathVariable String groupId,
    @RequestBody GroupAccessRequest request,
    @RequestHeader String tenantId) {
    log.info("Revoke group access: {}", request);
    groupService.revokeGroupAccess(groupId, request, tenantId);
    return ResponseEntity.ok("Group access revoked successfully");
  }

  @DeleteMapping("/{groupId}")
  public ResponseEntity<?> deleteGroup(
    @PathVariable String groupId,
    @RequestHeader String tenantId) {
    log.info("Delete group {} by {}", groupId, tenantId);
    groupService.deleteGroup(groupId, tenantId);
    return ResponseEntity.ok("Group deleted successfully");
  }

  @PutMapping("/{groupId}/transfer-ownership/{adminId}")
  public ResponseEntity<?> transferOwnership(
    @PathVariable String groupId,
    @PathVariable String adminId,
    @RequestHeader String tenantId) {
    log.info("Transfer ownership of group {}: {}", groupId, adminId);
    groupService.transferOwnership(groupId, adminId, tenantId);
    return ResponseEntity.ok("Ownership transferred successfully");
  }
}
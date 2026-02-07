package com.acl.project.controllers;

import com.acl.project.dto.ApiResponse;
import com.acl.project.dto.GroupAccessRequest;
import com.acl.project.services.GroupService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Slf4j
public class GroupController {

  private final GroupService groupService;

  @PostMapping
  public ResponseEntity<ApiResponse> createGroup(
    @RequestParam String groupId,
    HttpServletRequest httpServletRequest) {
    groupService.createGroup(groupId, httpServletRequest);
    return ResponseEntity.ok(ApiResponse.builder()
      .msg("Group created successfully")
      .groupId(groupId)
      .build());
  }

  @PostMapping("/{groupId}/members/{memberId}")
  public ResponseEntity<ApiResponse> addMember(
    @PathVariable String groupId,
    @PathVariable String memberId,
    HttpServletRequest httpServletRequest) {
    groupService.addGroupMember(groupId, memberId, httpServletRequest);
    return ResponseEntity.ok(ApiResponse.builder()
      .msg("Member added successfully")
      .groupId(groupId).adminId(memberId)
      .build());
  }

  @GetMapping("/{groupId}/members")
  public ResponseEntity<List<String>> getAllMembers(
    @PathVariable String groupId,
    HttpServletRequest httpServletRequest) {
    return ResponseEntity.ok(groupService.getAllMembers(groupId, httpServletRequest));
  }

  @DeleteMapping("/{groupId}/members/{memberId}")
  public ResponseEntity<ApiResponse> removeMember(
    @PathVariable String groupId,
    @PathVariable String memberId,
    HttpServletRequest httpServletRequest) {
    groupService.removeGroupMember(groupId, memberId, httpServletRequest);
    return ResponseEntity.ok(ApiResponse.builder()
      .msg("Member removed successfully")
      .groupId(groupId).adminId(memberId)
      .build());
  }

  @PostMapping("/{groupId}/access/grant")
  public ResponseEntity<ApiResponse> grantGroupAccess(
    @PathVariable String groupId,
    @RequestBody GroupAccessRequest request,
    HttpServletRequest httpServletRequest) {
    groupService.grantGroupAccess(groupId, request, httpServletRequest);
    return ResponseEntity.ok(ApiResponse.builder()
      .msg("Group access granted successfully")
      .groupId(groupId).requestBody(request)
      .build());
  }

  @DeleteMapping("/{groupId}/access/revoke")
  public ResponseEntity<ApiResponse> revokeGroupAccess(
    @PathVariable String groupId,
    @RequestBody GroupAccessRequest request,
    HttpServletRequest httpServletRequest) {
    groupService.revokeGroupAccess(groupId, request, httpServletRequest);
    return ResponseEntity.ok(ApiResponse.builder()
      .msg("Group access revoked successfully")
      .groupId(groupId).requestBody(request)
      .build());
  }

  @DeleteMapping("/{groupId}")
  public ResponseEntity<ApiResponse> deleteGroup(
    @PathVariable String groupId,
    HttpServletRequest httpServletRequest) {
    groupService.deleteGroup(groupId, httpServletRequest);
    return ResponseEntity.ok(ApiResponse.builder()
      .msg("Group deleted successfully")
      .groupId(groupId)
      .build());
  }


}
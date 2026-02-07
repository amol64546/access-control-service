package com.acl.project.controllers;

import com.acl.project.dto.*;
import com.acl.project.enums.Resource;
import com.acl.project.services.HierarchyService;
import com.acl.project.services.ResourceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@Slf4j
public class ResourceController {

  private final ResourceService resourceService;
  private final HierarchyService hierarchyService;

  @PostMapping
  public ResponseEntity<ApiResponse> create(
    @RequestBody CreateResource createResource,
    HttpServletRequest httpServletRequest) {
    log.info("Create Request: {}", createResource);
    resourceService.createResource(createResource, httpServletRequest);
    return ResponseEntity.ok(ApiResponse.builder()
      .requestBody(createResource).build());
  }

  @GetMapping
  public ResponseEntity<Boolean> checkPermission(
    @RequestBody PermissionCheckRequest permissionCheckRequest,
    HttpServletRequest httpServletRequest) {
    log.info("Permission Check Request: {}", permissionCheckRequest);
    return ResponseEntity.ok(resourceService.checkPermission(permissionCheckRequest, httpServletRequest));
  }

  @DeleteMapping
  public ResponseEntity<ApiResponse> delete(
    @RequestParam Resource resource,
    @RequestParam String resourceId,
    HttpServletRequest httpServletRequest) {
    log.info("Delete Request: {}", resource);
    resourceService.deleteResource(resource, resourceId, httpServletRequest);
    return ResponseEntity.ok(ApiResponse.builder()
      .msg("Delete the resource")
      .resourceId(resourceId).requestBody(resource)
      .build());

  }

  @PostMapping("/grant")
  public ResponseEntity<ApiResponse> grant(
    @RequestBody PermissionRequest permissionRequest,
    HttpServletRequest httpServletRequest) {
    log.info("Grant Request: {}", permissionRequest);
    resourceService.grantPermission(permissionRequest, httpServletRequest);
    return ResponseEntity.ok(ApiResponse.builder()
      .msg("Permission granted successfully")
      .requestBody(permissionRequest)
      .build());
  }

  @DeleteMapping("/revoke")
  public ResponseEntity<ApiResponse> Revoke(
    @RequestBody PermissionRequest permissionRequest,
    HttpServletRequest httpServletRequest) {
    log.info("Revoke Request: {}", permissionRequest);
    resourceService.revokePermission(permissionRequest, httpServletRequest);
    return ResponseEntity.ok(ApiResponse.builder()
      .msg("Permission revoked successfully")
      .requestBody(permissionRequest)
      .build());
  }

  @GetMapping("/hierarchy")
  public ResponseEntity<HierarchyResponse> getCompleteHierarchy(
    @RequestParam Resource resource,
    @RequestParam String resourceId,
    HttpServletRequest httpServletRequest) {
    log.info("Get complete hierarchy for {}:{}", resource, resourceId);
    return ResponseEntity.ok(
      hierarchyService.getCompleteHierarchy(resource, resourceId, httpServletRequest)
    );
  }
}
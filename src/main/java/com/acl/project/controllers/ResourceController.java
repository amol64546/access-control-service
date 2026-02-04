package com.acl.project.controllers;

import com.acl.project.dto.*;
import com.acl.project.services.HierarchyService;
import com.acl.project.services.ResourceService;
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
  public ResponseEntity<?> create(
    @RequestBody CreateResource createResource) {
    log.info("Create Request: {}", createResource);
    return ResponseEntity.ok(resourceService.createResource(createResource));
  }

  @GetMapping
  public ResponseEntity<Boolean> checkPermission(
    @RequestBody PermissionCheckRequest permissionCheckRequest) {
    log.info("Permission Check Request: {}", permissionCheckRequest);
    return ResponseEntity.ok(resourceService.checkPermission(permissionCheckRequest));
  }

  @DeleteMapping
  public ResponseEntity<?> delete(
    @RequestBody DeleteResource deleteResource) {
    log.info("Delete Request: {}", deleteResource);
    return resourceService.deleteResource(deleteResource);
  }

  @PostMapping("/grant")
  public ResponseEntity<?> grant(
    @RequestBody AccessRequest accessRequest) {
    log.info("Grant Request: {}", accessRequest);
    return resourceService.grantPermission(accessRequest);
  }

  @DeleteMapping("/revoke")
  public ResponseEntity<?> Revoke(
    @RequestBody AccessRequest accessRequest) {
    log.info("Revoke Request: {}", accessRequest);
    return resourceService.revokePermission(accessRequest);
  }

  @GetMapping("/hierarchy")
  public ResponseEntity<HierarchyResponse> getCompleteHierarchy(
    @RequestParam String resourceType,
    @RequestParam String resourceId,
    @RequestParam String requesterId) {
    log.info("Get complete hierarchy for {}:{}", resourceType, resourceId);
    return ResponseEntity.ok(
      hierarchyService.getCompleteHierarchy(resourceType, resourceId, requesterId)
    );
  }
}
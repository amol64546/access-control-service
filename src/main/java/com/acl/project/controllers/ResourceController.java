package com.acl.project.controllers;

import com.acl.project.dto.CreateResource;
import com.acl.project.dto.HierarchyResponse;
import com.acl.project.dto.PermissionCheckRequest;
import com.acl.project.dto.PermissionRequest;
import com.acl.project.enums.Resource;
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
    @RequestBody CreateResource createResource,
    @RequestHeader String tenantId) {
    log.info("Create Request: {}", createResource);
    return ResponseEntity.ok(resourceService.createResource(createResource, tenantId));
  }

  @GetMapping
  public ResponseEntity<Boolean> checkPermission(
    @RequestBody PermissionCheckRequest permissionCheckRequest,
    @RequestHeader String tenantId) {
    log.info("Permission Check Request: {}", permissionCheckRequest);
    return ResponseEntity.ok(resourceService.checkPermission(permissionCheckRequest, tenantId));
  }

  @DeleteMapping
  public ResponseEntity<?> delete(
    @RequestParam Resource resource,
    @RequestParam String resourceId,
    @RequestHeader String tenantId) {
    log.info("Delete Request: {}", resource);
    return resourceService.deleteResource(resource, resourceId, tenantId);
  }

  @PostMapping("/grant")
  public ResponseEntity<?> grant(
    @RequestBody PermissionRequest permissionRequest,
    @RequestHeader String tenantId) {
    log.info("Grant Request: {}", permissionRequest);
    return resourceService.grantPermission(permissionRequest, tenantId);
  }

  @DeleteMapping("/revoke")
  public ResponseEntity<?> Revoke(
    @RequestBody PermissionRequest permissionRequest,
    @RequestHeader String tenantId) {
    log.info("Revoke Request: {}", permissionRequest);
    return resourceService.revokePermission(permissionRequest, tenantId);
  }

  @GetMapping("/hierarchy")
  public ResponseEntity<HierarchyResponse> getCompleteHierarchy(
    @RequestParam Resource resource,
    @RequestParam String resourceId,
    @RequestHeader String tenantId) {
    log.info("Get complete hierarchy for {}:{}", resource, resourceId);
    return ResponseEntity.ok(
      hierarchyService.getCompleteHierarchy(resource, resourceId, tenantId)
    );
  }
}
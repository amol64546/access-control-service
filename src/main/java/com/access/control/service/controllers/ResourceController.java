package com.access.control.service.controllers;

import com.access.control.service.dto.*;
import com.acl.project.dto.*;
import com.access.control.service.enums.Resource;
import com.access.control.service.services.HierarchyService;
import com.access.control.service.services.ResourceService;
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
      .msg("Resource created successfully")
      .requestBody(createResource).build());
  }

  @GetMapping
  public ResponseEntity<ApiResponse> checkPermission(
    @RequestBody PermissionCheckRequest permissionCheckRequest,
    HttpServletRequest httpServletRequest) {
    log.info("Permission Check Request: {}", permissionCheckRequest);
    boolean allowed = resourceService.checkPermission(permissionCheckRequest, httpServletRequest);
    return ResponseEntity.ok(ApiResponse.builder()
      .msg(allowed ? "Permission granted" : "Permission denied")
      .allowed(allowed).build());
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
      .resourceId(resourceId).resource(resource)
      .build());

  }

  @PostMapping("/grant")
  public ResponseEntity<ApiResponse> grant(
    @RequestBody PermissionAccessRequest permissionAccessRequest,
    HttpServletRequest httpServletRequest) {
    log.info("Grant Request: {}", permissionAccessRequest);
    resourceService.grantPermission(permissionAccessRequest, httpServletRequest);
    return ResponseEntity.ok(ApiResponse.builder()
      .msg("Permission granted successfully")
      .requestBody(permissionAccessRequest)
      .build());
  }

  @DeleteMapping("/revoke")
  public ResponseEntity<ApiResponse> Revoke(
    @RequestBody PermissionAccessRequest permissionAccessRequest,
    HttpServletRequest httpServletRequest) {
    log.info("Revoke Request: {}", permissionAccessRequest);
    resourceService.revokePermission(permissionAccessRequest, httpServletRequest);
    return ResponseEntity.ok(ApiResponse.builder()
      .msg("Permission revoked successfully")
      .requestBody(permissionAccessRequest)
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
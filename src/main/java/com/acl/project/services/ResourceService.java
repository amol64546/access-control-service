package com.acl.project.services;

import com.acl.project.dto.CreateResource;
import com.acl.project.dto.PermissionCheckRequest;
import com.acl.project.dto.PermissionRequest;
import com.acl.project.enums.Permission;
import com.acl.project.enums.Relation;
import com.acl.project.enums.Resource;
import com.acl.project.enums.Subject;
import com.acl.project.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static com.acl.project.utils.constants.TENANT;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceService {

  private final AuthorizationService authorizationService;

  public Object createResource(CreateResource request, String tenantId) {
    authorizationService.writeRelationship(
      request.getResource(), request.getResourceId(),
      Relation.OWNER, TENANT, tenantId
    );

    // Set parent hierarchy if provided
    if (StringUtils.isNotBlank(request.getParentResource())
      && StringUtils.isNotBlank(request.getParentResourceId())) {

      authorizationService.writeRelationship(
        request.getResource(),
        request.getResourceId(),
        Relation.PARENT,
        request.getParentResource(),
        request.getParentResourceId()
      );

      log.info("Created hierarchy: {} -> {} ({}:{})",
        request.getResource() + ":" + request.getResourceId(),
        Relation.PARENT,
        request.getParentResource(),
        request.getParentResourceId()
      );
    }
    return request;
  }

  public boolean checkPermission(PermissionCheckRequest request, String tenantId) {
    if (ObjectUtils.isEmpty(request.getConditionalPermission())) {
      return authorizationService.checkPermission(
        request.getResource(), request.getResourceId(),
        request.getPermission(), Subject.TENANT, tenantId
      );
    } else {
      return authorizationService.checkPermission(
        request.getResource(), request.getResourceId(),
        request.getPermission(), Subject.TENANT, tenantId,
        request.getConditionalPermission()
      );
    }
  }

  public ResponseEntity<String> deleteResource(Resource resource, String resourceId, String tenantId) {
    validatePermission(tenantId, resourceId,
      resource, Permission.DELETE);
    authorizationService.deleteRelationship(
      resource, resourceId);
    return ResponseEntity.ok("Delete the resource.");
  }

  public ResponseEntity<String> grantPermission(PermissionRequest request, String tenantId) {
    validateRelation(request.getRelation());
    validatePermission(tenantId, request.getResourceId(),
      request.getResource(), Permission.GRANT);
    if (ObjectUtils.isEmpty(request.getConditionalPermission())) {
      authorizationService.writeRelationship(
        request.getResource(), request.getResourceId(),
        request.getRelation(), TENANT, request.getUserId());
    } else {
      authorizationService.writeRelationship(
        request.getResource(), request.getResourceId(),
        request.getRelation(), Subject.TENANT, request.getUserId(),
        request.getConditionalPermission());
    }

    return ResponseEntity.ok("Permission granted successfully.");
  }

  public ResponseEntity<String> revokePermission(PermissionRequest request, String tenantId) {
    validateRelation(request.getRelation());
    validatePermission(tenantId, request.getResourceId(),
      request.getResource(), Permission.REVOKE);
    authorizationService.deleteRelationship(
      request.getResource(), request.getResourceId(),
      request.getRelation(), Subject.TENANT, request.getUserId());
    return ResponseEntity.ok("Permission granted successfully.");
  }

  private void validateRelation(Relation relation) {
    if (relation.equals(Relation.OWNER)) {
      throw new ApiException(HttpStatus.BAD_REQUEST,
        "Subject is the owner of resource.");
    }
  }

  private void validatePermission(String subjectId, String resourceId,
                                  Resource resourceType, Permission permission) {
    if (!authorizationService.checkPermission(resourceType, resourceId,
      permission, Subject.TENANT, subjectId)) {
      throw new ApiException(HttpStatus.FORBIDDEN,
        "Subject does not have %s permission.".formatted(permission));
    }
  }


}

package com.acl.project.services;

import com.acl.project.dto.AccessRequest;
import com.acl.project.dto.CreateResource;
import com.acl.project.dto.DeleteResource;
import com.acl.project.dto.PermissionCheckRequest;
import com.acl.project.exception.ApiException;
import com.acl.project.exception.ErrorObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static com.acl.project.utils.constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceService {

  private final AuthorizationService authorizationService;

  public Object createResource(CreateResource request) {
    authorizationService.writeRelationship(
      request.getResourceType(), request.getResourceId(),
      OWNER, TENANT, request.getRequesterId()
    );

    // Set parent hierarchy if provided
    if (StringUtils.isNotBlank(request.getParentResourceType())
      && StringUtils.isNotBlank(request.getParentResourceId())) {

      authorizationService.writeRelationship(
        request.getResourceType(),
        request.getResourceId(),
        PARENT,
        request.getParentResourceType(),
        request.getParentResourceId()
      );

      log.info("Created hierarchy: {} -> {} ({}:{})",
        request.getResourceType() + ":" + request.getResourceId(),
        PARENT,
        request.getParentResourceType(),
        request.getParentResourceId()
      );
    }
    return request;
  }

  public boolean checkPermission(PermissionCheckRequest request) {
    if (ObjectUtils.isEmpty(request.getConditionalPermission())) {
      return authorizationService.checkPermission(
        request.getResourceType(), request.getResourceId(),
        request.getPermission(), TENANT, request.getRequesterId()
      );
    } else {
      return authorizationService.checkPermission(
        request.getResourceType(), request.getResourceId(),
        request.getPermission(), TENANT, request.getRequesterId(),
        request.getConditionalPermission()
      );
    }
  }

  public ResponseEntity<String> deleteResource(DeleteResource request) {
    validatePermission(request.getRequesterId(), request.getResourceId(),
      request.getResourceType(), DELETE);
    authorizationService.deleteRelationship(
      request.getResourceType(), request.getResourceId());
    return ResponseEntity.ok("Delete the resource.");
  }

  public ResponseEntity<String> grantPermission(AccessRequest request) {
    validateRelation(request.getRelation());
    validatePermission(request.getOwnerSubjectId(), request.getResourceId(),
      request.getResourceType(), GRANT);
    if (ObjectUtils.isEmpty(request.getConditionalPermission())) {
      authorizationService.writeRelationship(
        request.getResourceType(), request.getResourceId(),
        request.getRelation(), TENANT, request.getTargetSubjectId());
    } else {
      authorizationService.writeRelationship(
        request.getResourceType(), request.getResourceId(),
        request.getRelation(), TENANT, request.getTargetSubjectId(),
        request.getConditionalPermission());
    }

    return ResponseEntity.ok("Permission granted successfully.");
  }

  public ResponseEntity<String> revokePermission(AccessRequest request) {
    validateRelation(request.getRelation());
    validatePermission(request.getOwnerSubjectId(), request.getResourceId(),
      request.getResourceType(), REVOKE);
    authorizationService.deleteRelationship(
      request.getResourceType(), request.getResourceId(),
      request.getRelation(), TENANT, request.getTargetSubjectId());
    return ResponseEntity.ok("Permission granted successfully.");
  }

  private void validateRelation(String relation) {
    if (relation.equalsIgnoreCase(OWNER)) {
      throw new ApiException(ErrorObject.builder()
        .httpStatus(HttpStatus.BAD_REQUEST)
        .errorMessage("Subject is the owner of resource.")
        .build());
    }
  }

  private void validatePermission(String subjectId, String resourceId,
                                  String resourceType, String permission) {
    if (!authorizationService.checkPermission(resourceType, resourceId,
      permission, TENANT, subjectId)) {
      throw new ApiException(ErrorObject.builder()
        .httpStatus(HttpStatus.FORBIDDEN)
        .errorMessage("Subject does not have %s permission.".formatted(permission))
        .build());
    }
  }


}

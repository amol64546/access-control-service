package com.acl.project.services;

import com.acl.project.dto.CreateResource;
import com.acl.project.dto.PermissionCheckRequest;
import com.acl.project.dto.PermissionRequest;
import com.acl.project.enums.Permission;
import com.acl.project.enums.Relation;
import com.acl.project.enums.Resource;
import com.acl.project.enums.Subject;
import com.acl.project.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
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

  public void createResource(CreateResource request, HttpServletRequest httpServletRequest) {

    String tenantId = httpServletRequest.getHeader(TENANT_ID);

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
  }

  public boolean checkPermission(PermissionCheckRequest request, HttpServletRequest httpServletRequest) {
    String tenantId = httpServletRequest.getHeader(TENANT_ID);
    String password = httpServletRequest.getHeader(PASSWORD);

    if (StringUtils.isBlank(password)) {
      return authorizationService.checkPermission(
        request.getResource(), request.getResourceId(),
        request.getPermission(), Subject.TENANT, tenantId
      );
    } else {
      return authorizationService.checkPermission(
        request.getResource(), request.getResourceId(),
        request.getPermission(), Subject.TENANT, tenantId,
        password
      );
    }
  }

  public void deleteResource(Resource resource, String resourceId, HttpServletRequest httpServletRequest) {
    String tenantId = httpServletRequest.getHeader(TENANT_ID);

    validatePermission(tenantId, resourceId,
      resource, Permission.DELETE);
    authorizationService.deleteRelationship(
      resource, resourceId);
  }

  public void grantPermission(PermissionRequest request, HttpServletRequest httpServletRequest) {
    String tenantId = httpServletRequest.getHeader(TENANT_ID);
    String password = httpServletRequest.getHeader(PASSWORD);

    validateRelation(request.getRelation());
    validatePermission(tenantId, request.getResourceId(),
      request.getResource(), Permission.GRANT);
    if (StringUtils.isBlank(password)) {
      authorizationService.writeRelationship(
        request.getResource(), request.getResourceId(),
        request.getRelation(), TENANT, request.getUserId());
    } else {
      authorizationService.writeRelationship(
        request.getResource(), request.getResourceId(),
        request.getRelation(), Subject.TENANT, request.getUserId(),
        password);
    }

  }

  public void revokePermission(PermissionRequest request, HttpServletRequest httpServletRequest) {
    String tenantId = httpServletRequest.getHeader(TENANT_ID);

    validateRelation(request.getRelation());
    validatePermission(tenantId, request.getResourceId(),
      request.getResource(), Permission.REVOKE);
    authorizationService.deleteRelationship(
      request.getResource(), request.getResourceId(),
      request.getRelation(), Subject.TENANT, request.getUserId());
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

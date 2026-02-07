package com.access.control.service.services;

import com.access.control.service.builders.PermissionOptions;
import com.access.control.service.builders.RelationshipOptions;
import com.access.control.service.dto.CreateResource;
import com.access.control.service.dto.PermissionAccessRequest;
import com.access.control.service.dto.PermissionCheckRequest;
import com.access.control.service.enums.Permission;
import com.access.control.service.enums.Relation;
import com.access.control.service.enums.Resource;
import com.access.control.service.enums.Subject;
import com.access.control.service.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static com.access.control.service.utils.constants.PASSWORD;
import static com.access.control.service.utils.constants.TENANT_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceService {

  private final AuthorizationService authorizationService;

  public void createResource(CreateResource request, HttpServletRequest httpServletRequest) {

    String tenantId = httpServletRequest.getHeader(TENANT_ID);

    // Create ownership relationship
    authorizationService.writeRelationship(
      RelationshipOptions.builder()
        .resource(request.getResource())
        .resourceId(request.getResourceId())
        .relation(Relation.OWNER)
        .subject(Subject.TENANT)
        .subjectId(tenantId)
        .build()
    );

    // Set parent hierarchy if provided
    if (StringUtils.isNotBlank(request.getParentResource())
      && StringUtils.isNotBlank(request.getParentResourceId())) {

      authorizationService.writeRelationship(
        RelationshipOptions.builder()
          .resource(request.getResource())
          .resourceId(request.getResourceId())
          .relation(Relation.PARENT)
          .subject(Subject.valueOf(request.getParentResource().toUpperCase()))
          .subjectId(request.getParentResourceId())
          .build()
      );
    }
  }

  public boolean checkPermission(PermissionCheckRequest request, HttpServletRequest httpServletRequest) {
    String tenantId = httpServletRequest.getHeader(TENANT_ID);
    String password = httpServletRequest.getHeader(PASSWORD);

    return authorizationService.checkPermission(
      PermissionOptions.builder()
        .resource(request.getResource())
        .resourceId(request.getResourceId())
        .permission(request.getPermission())
        .subject(Subject.TENANT)
        .subjectId(tenantId)
        .password(password)  // Can be null
        .build()
    );
  }

  public void deleteResource(Resource resource, String resourceId, HttpServletRequest httpServletRequest) {
    String tenantId = httpServletRequest.getHeader(TENANT_ID);

    validatePermission(tenantId, resourceId, resource, Permission.DELETE);
    authorizationService.deleteRelationship(
      resource, resourceId);
  }

  public void grantPermission(PermissionAccessRequest request, HttpServletRequest httpServletRequest) {
    String tenantId = httpServletRequest.getHeader(TENANT_ID);

    validateRelation(request.getRelation());
    validatePermission(tenantId, request.getResourceId(),
      request.getResource(), Permission.GRANT);

    authorizationService.writeRelationship(
      RelationshipOptions.builder()
        .resource(request.getResource())
        .resourceId(request.getResourceId())
        .relation(request.getRelation())
        .subject(Subject.TENANT)
        .subjectId(request.getUserId())
        .password(request.getPassword())
        .daysFromNow(request.getDaysFromNow())
        .build()
    );

  }

  public void revokePermission(PermissionAccessRequest request, HttpServletRequest httpServletRequest) {
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

    if (!authorizationService.checkPermission(PermissionOptions.builder()
      .resource(resourceType).resourceId(resourceId)
      .subject(Subject.TENANT).subjectId(subjectId)
      .permission(permission).build())) {
      throw new ApiException(HttpStatus.FORBIDDEN,
        "Subject does not have %s permission.".formatted(permission));
    }
  }


}

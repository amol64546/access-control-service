package com.acl.project.services;

import com.acl.project.dto.HierarchyRelation;
import com.acl.project.dto.HierarchyResponse;
import com.acl.project.dto.HierarchySummary;
import com.acl.project.dto.RelationshipInfo;
import com.acl.project.enums.*;
import com.acl.project.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.acl.project.utils.constants.ROOT_RESOURCE;
import static com.acl.project.utils.constants.TENANT_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HierarchyService {

  private final AuthorizationService authorizationService;

  public HierarchyResponse getCompleteHierarchy(Resource resource,
                                                String resourceId,
                                                HttpServletRequest httpServletRequest) {
    log.info("Getting complete hierarchy for {}:{}", resource, resourceId);
    String tenantId = httpServletRequest.getHeader(TENANT_ID);

    if (!authorizationService.checkPermission(resource, resourceId,
      Permission.READ, Subject.TENANT, tenantId)) {
      throw new ApiException(HttpStatus.FORBIDDEN,
        "Subject does not have view permission.");
    }

    List<HierarchyRelation> allRelations = new ArrayList<>();

    // Get all parent relationships (going up the tree)
    allRelations.addAll(getAllParents(resource, resourceId));

    // Get all child relationships (going down the tree)
    allRelations.addAll(getAllChildren(resource, resourceId, tenantId));

    // Calculate summary
    HierarchySummary summary = calculateSummary(allRelations);

    return HierarchyResponse.builder()
      .resource(resource)
      .resourceId(resourceId)
      .relations(allRelations)
      .summary(summary)
      .build();
  }

  /**
   * Get all parents recursively
   */
  private List<HierarchyRelation> getAllParents(Resource resource, String resourceId) {
    List<HierarchyRelation> parents = new ArrayList<>();
    Set<String> visited = new HashSet<>();

    traverseParents(resource, resourceId, 1, parents, visited);

    return parents;
  }

  private void traverseParents(Resource resource, String resourceId,
                               int level, List<HierarchyRelation> parents, Set<String> visited) {
    String key = resource + ":" + resourceId;
    if (visited.contains(key)) {
      return; // Prevent infinite loops
    }
    visited.add(key);

    // Get all outgoing relations for this resource
    List<RelationshipInfo> outgoingRelations = new ArrayList<>();
    if (!resource.equals(ROOT_RESOURCE)) {
      outgoingRelations =
        authorizationService.getOutgoingRelations(resource, resourceId, Relation.PARENT);
    }

    for (RelationshipInfo rel : outgoingRelations) {
      AccessType accessType = (level == 1) ? AccessType.DIRECT : AccessType.INDIRECT;

      parents.add(HierarchyRelation.builder()
        .accessType(accessType)
        .resource(rel.getToResource())
        .resourceId(rel.getToResourceId())
        .level(level)
        .relation(Relation.PARENT)
        .build());

      // Recursively traverse up
      traverseParents(rel.getToResource(), rel.getToResourceId(),
        level + 1, parents, visited);
    }
  }

  /**
   * Get all children recursively
   */
  private List<HierarchyRelation> getAllChildren(Resource resource, String resourceId, String requesterId) {
    List<HierarchyRelation> children = new ArrayList<>();
    Set<String> visited = new HashSet<>();

    traverseChildren(resource, resourceId, requesterId, 1, children, visited);

    return children;
  }

  private void traverseChildren(Resource resource, String resourceId, String userId,
                                int level, List<HierarchyRelation> children, Set<String> visited) {
    String key = resource + ":" + resourceId;
    if (visited.contains(key)) {
      return;
    }
    visited.add(key);

    // Get all resources of this type that have this resource as parent
    List<RelationshipInfo> incomingRelations =
      authorizationService.getIncomingRelations(resource, resourceId, Relation.PARENT);

    for (RelationshipInfo rel : incomingRelations) {
      // Check if user has permission to view this child
      if (!authorizationService.checkPermission(rel.getResource(), rel.getResourceId(),
        Permission.READ, Subject.TENANT, userId)) {
        continue;
      }

      AccessType accessType = (level == 1) ? AccessType.DIRECT : AccessType.INDIRECT;

      children.add(HierarchyRelation.builder()
        .accessType(accessType)
        .resource(rel.getResource())
        .resourceId(rel.getResourceId())
        .level(level)
        .relation(Relation.CHILD)
        .build());

      // Recursively traverse down
      traverseChildren(rel.getResource(), rel.getResourceId(), userId,
        level + 1, children, visited);
    }
  }

  /**
   * Calculate summary statistics
   */
  private HierarchySummary calculateSummary(List<HierarchyRelation> relations) {
    long directParents = relations.stream()
      .filter(r -> Relation.PARENT.equals(r.getRelation())
        && AccessType.DIRECT.equals(r.getAccessType()))
      .count();

    long directChildren = relations.stream()
      .filter(r -> Relation.CHILD.equals(r.getRelation())
        && AccessType.DIRECT.equals(r.getAccessType()))
      .count();

    long indirectParents = relations.stream()
      .filter(r -> Relation.PARENT.equals(r.getRelation())
        && AccessType.INDIRECT.equals(r.getAccessType()))
      .count();

    long indirectChildren = relations.stream()
      .filter(r -> Relation.CHILD.equals(r.getRelation())
        && AccessType.INDIRECT.equals(r.getAccessType()))
      .count();

    int maxParentLevel = relations.stream()
      .filter(r -> r.getRelation().equals(Relation.PARENT))
      .mapToInt(HierarchyRelation::getLevel)
      .max()
      .orElse(0);

    int maxChildLevel = relations.stream()
      .filter(r -> r.getRelation().equals(Relation.CHILD))
      .mapToInt(HierarchyRelation::getLevel)
      .max()
      .orElse(0);

    return HierarchySummary.builder()
      .totalRelations(relations.size())
      .directParents((int) directParents)
      .directChildren((int) directChildren)
      .indirectParents((int) indirectParents)
      .indirectChildren((int) indirectChildren)
      .maxParentLevel(maxParentLevel)
      .maxChildLevel(maxChildLevel)
      .build();
  }
}
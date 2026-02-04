package com.acl.project.services;

import com.acl.project.dto.HierarchyRelation;
import com.acl.project.dto.HierarchyResponse;
import com.acl.project.dto.HierarchySummary;
import com.acl.project.dto.RelationshipInfo;
import com.acl.project.exception.ApiException;
import com.acl.project.exception.ErrorObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.acl.project.utils.constants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class HierarchyService {

  private final AuthorizationService authorizationService;

  public HierarchyResponse getCompleteHierarchy(String resourceType,
                                                String resourceId,
                                                String requesterId) {
    log.info("Getting complete hierarchy for {}:{}", resourceType, resourceId);

    if (!authorizationService.checkPermission(resourceType, resourceId,
      READ, TENANT, requesterId)) {
      throw new ApiException(ErrorObject.builder()
        .httpStatus(HttpStatus.FORBIDDEN)
        .errorMessage("Subject does not have view permission.")
        .build());
    }

    List<HierarchyRelation> allRelations = new ArrayList<>();

    // Get all parent relationships (going up the tree)
    allRelations.addAll(getAllParents(resourceType, resourceId));

    // Get all child relationships (going down the tree)
    allRelations.addAll(getAllChildren(resourceType, resourceId, requesterId));

    // Calculate summary
    HierarchySummary summary = calculateSummary(allRelations);

    return HierarchyResponse.builder()
      .resourceType(resourceType)
      .resourceId(resourceId)
      .relations(allRelations)
      .summary(summary)
      .build();
  }

  /**
   * Get all parents recursively
   */
  private List<HierarchyRelation> getAllParents(String resourceType, String resourceId) {
    List<HierarchyRelation> parents = new ArrayList<>();
    Set<String> visited = new HashSet<>();

    traverseParents(resourceType, resourceId, 1, parents, visited);

    return parents;
  }

  private void traverseParents(String resourceType, String resourceId,
                               int level, List<HierarchyRelation> parents, Set<String> visited) {
    String key = resourceType + ":" + resourceId;
    if (visited.contains(key)) {
      return; // Prevent infinite loops
    }
    visited.add(key);

    // Get all outgoing relations for this resource
    List<RelationshipInfo> outgoingRelations = new ArrayList<>();
    if (!resourceType.equals(ROOT_RESOURCE)) {
      outgoingRelations =
        authorizationService.getOutgoingRelations(resourceType, resourceId, PARENT);
    }

    for (RelationshipInfo rel : outgoingRelations) {
      String relationType = (level == 1) ? DIRECT : INDIRECT;

      parents.add(HierarchyRelation.builder()
        .relationType(relationType)
        .resourceType(rel.getToResourceType())
        .resourceId(rel.getToResourceId())
        .level(level)
        .relation(PARENT)
        .build());

      // Recursively traverse up
      traverseParents(rel.getToResourceType(), rel.getToResourceId(),
        level + 1, parents, visited);
    }
  }

  /**
   * Get all children recursively
   */
  private List<HierarchyRelation> getAllChildren(String resourceType, String resourceId, String requesterId) {
    List<HierarchyRelation> children = new ArrayList<>();
    Set<String> visited = new HashSet<>();

    traverseChildren(resourceType, resourceId, requesterId, 1, children, visited);

    return children;
  }

  private void traverseChildren(String resourceType, String resourceId, String userId,
                                int level, List<HierarchyRelation> children, Set<String> visited) {
    String key = resourceType + ":" + resourceId;
    if (visited.contains(key)) {
      return;
    }
    visited.add(key);

    // Get all resources of this type that have this resource as parent
    List<RelationshipInfo> incomingRelations =
      authorizationService.getIncomingRelations(resourceType, resourceId, PARENT);

    for (RelationshipInfo rel : incomingRelations) {
      // Check if user has permission to view this child
      if (!authorizationService.checkPermission(rel.getResourceType(), rel.getResourceId(),
        READ, TENANT, userId)) {
        continue;
      }

      String relationType = (level == 1) ? DIRECT : INDIRECT;

      children.add(HierarchyRelation.builder()
        .relationType(relationType)
        .resourceType(rel.getResourceType())
        .resourceId(rel.getResourceId())
        .level(level)
        .relation(CHILD)
        .build());

      // Recursively traverse down
      traverseChildren(rel.getResourceType(), rel.getResourceId(), userId,
        level + 1, children, visited);
    }
  }

  /**
   * Calculate summary statistics
   */
  private HierarchySummary calculateSummary(List<HierarchyRelation> relations) {
    long directParents = relations.stream()
      .filter(r -> PARENT.equals(r.getRelation()) && DIRECT.equals(r.getRelationType()))
      .count();

    long directChildren = relations.stream()
      .filter(r -> CHILD.equals(r.getRelation()) && DIRECT.equals(r.getRelationType()))
      .count();

    long indirectParents = relations.stream()
      .filter(r -> PARENT.equals(r.getRelation()) && INDIRECT.equals(r.getRelationType()))
      .count();

    long indirectChildren = relations.stream()
      .filter(r -> CHILD.equals(r.getRelation()) && INDIRECT.equals(r.getRelationType()))
      .count();

    int maxParentLevel = relations.stream()
      .filter(r -> r.getRelation().contains(PARENT))
      .mapToInt(HierarchyRelation::getLevel)
      .max()
      .orElse(0);

    int maxChildLevel = relations.stream()
      .filter(r -> r.getRelation().contains(CHILD))
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
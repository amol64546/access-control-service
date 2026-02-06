package com.acl.project.services;

import com.acl.project.dto.ConditionalPermissionRequest;
import com.acl.project.dto.RelationshipInfo;
import com.authzed.api.v1.*;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.acl.project.utils.constants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {

  private final PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsClient;

  public void writeRelationship(String resourceType, String resourceId,
                                String relation, String subjectType, String subjectId) {
    WriteRelationshipsRequest request = WriteRelationshipsRequest.newBuilder()
      .addUpdates(RelationshipUpdate.newBuilder()
        .setOperation(RelationshipUpdate.Operation.OPERATION_TOUCH)
        .setRelationship(Relationship.newBuilder()
          .setResource(ObjectReference.newBuilder()
            .setObjectType(resourceType)
            .setObjectId(resourceId)
            .build())
          .setRelation(relation)
          .setSubject(SubjectReference.newBuilder()
            .setObject(ObjectReference.newBuilder()
              .setObjectType(subjectType)
              .setObjectId(subjectId)
              .build())
            .build())
          .build())
        .build())
      .build();

    permissionsClient.writeRelationships(request);
  }


  public void writeRelationship(
    String resourceType,
    String resourceId,
    String relation,
    String subjectType,
    String subjectId,
    ConditionalPermissionRequest conditionalPermissionRequest
  ) {

    // Build caveat context (Struct)
    Struct.Builder contextBuilder = Struct.newBuilder();
    if (StringUtils.isNotBlank(conditionalPermissionRequest.getPlatformId())) {
      contextBuilder.putFields(CAVEAT_ALLOWED_KEY, Value.newBuilder().setStringValue(conditionalPermissionRequest.getPlatformId()).build());
    }

    Relationship relationship =
      Relationship.newBuilder()
        .setResource(
          ObjectReference.newBuilder()
            .setObjectType(resourceType)
            .setObjectId(resourceId)
            .build())
        .setRelation(relation)
        .setSubject(
          SubjectReference.newBuilder()
            .setObject(
              ObjectReference.newBuilder()
                .setObjectType(subjectType)
                .setObjectId(subjectId)
                .build())
            .build())
        .setOptionalCaveat(
          ContextualizedCaveat.newBuilder()
            .setCaveatName(CAVEAT_NAME)
            .setContext(contextBuilder.build())
            .build())
        .build();

    WriteRelationshipsRequest request =
      WriteRelationshipsRequest.newBuilder()
        .addUpdates(
          RelationshipUpdate.newBuilder()
            .setOperation(RelationshipUpdate.Operation.OPERATION_TOUCH)
            .setRelationship(relationship)
            .build())
        .build();

    permissionsClient.writeRelationships(request);
  }


  public boolean checkPermission(String resourceType, String resourceId,
                                 String permission, String requesterType, String requesterId) {
    CheckPermissionRequest request = CheckPermissionRequest.newBuilder()
      .setResource(ObjectReference.newBuilder()
        .setObjectType(resourceType)
        .setObjectId(resourceId)
        .build())
      .setPermission(permission)
      .setSubject(SubjectReference.newBuilder()
        .setObject(ObjectReference.newBuilder()
          .setObjectType(requesterType)
          .setObjectId(requesterId)
          .build())
        .build())
      .build();

    CheckPermissionResponse response = permissionsClient.checkPermission(request);
    return response.getPermissionship() ==
      CheckPermissionResponse.Permissionship.PERMISSIONSHIP_HAS_PERMISSION;
  }

  public boolean checkPermission(
    String resourceType,
    String resourceId,
    String permission,
    String requesterType,
    String requesterId,
    ConditionalPermissionRequest conditionalPermissionRequest
  ) {

    Struct.Builder contextBuilder = Struct.newBuilder();

    if (StringUtils.isNotBlank(conditionalPermissionRequest.getPlatformId())) {
      contextBuilder.putFields(
        CAVEAT_SUPPLIED_KEY,
        Value.newBuilder().setStringValue(conditionalPermissionRequest.getPlatformId()).build()
      );
    }

    CheckPermissionRequest request =
      CheckPermissionRequest.newBuilder()
        .setResource(
          ObjectReference.newBuilder()
            .setObjectType(resourceType)
            .setObjectId(resourceId)
            .build())
        .setPermission(permission)
        .setSubject(
          SubjectReference.newBuilder()
            .setObject(
              ObjectReference.newBuilder()
                .setObjectType(requesterType)
                .setObjectId(requesterId)
                .build())
            .build())
        .setContext(contextBuilder.build())
        .build();

    CheckPermissionResponse response =
      permissionsClient.checkPermission(request);
    return response.getPermissionship() ==
      CheckPermissionResponse.Permissionship.PERMISSIONSHIP_HAS_PERMISSION;
  }


  public void deleteRelationship(String resourceType, String resourceId) {
    DeleteRelationshipsRequest request =
      DeleteRelationshipsRequest.newBuilder()
        .setRelationshipFilter(
          RelationshipFilter.newBuilder()
            .setResourceType(resourceType)
            .setOptionalResourceId(resourceId)
            .build()
        )
        .build();

    permissionsClient.deleteRelationships(request);
  }

  public void deleteRelationship(String resourceType, String resourceId, String relation,
                                 String subjectType, String subjectId) {
    DeleteRelationshipsRequest request = DeleteRelationshipsRequest.newBuilder()
      .setRelationshipFilter(RelationshipFilter.newBuilder()
        .setResourceType(resourceType)
        .setOptionalResourceId(resourceId)
        .setOptionalRelation(relation)
        .setOptionalSubjectFilter(SubjectFilter.newBuilder()
          .setSubjectType(subjectType)
          .setOptionalSubjectId(subjectId)
          .build())
        .build())
      .build();

    permissionsClient.deleteRelationships(request);
  }


  public List<RelationshipInfo> getOutgoingRelations(String resourceType, String resourceId,
                                                     String relation) {
    ReadRelationshipsRequest request = ReadRelationshipsRequest.newBuilder()
      .setRelationshipFilter(RelationshipFilter.newBuilder()
        .setResourceType(resourceType)
        .setOptionalResourceId(resourceId)
        .setOptionalRelation(relation)
        .build())
      .build();

    List<RelationshipInfo> relations = new ArrayList<>();
    Iterator<ReadRelationshipsResponse> responses = permissionsClient.readRelationships(request);

    while (responses.hasNext()) {
      ReadRelationshipsResponse response = responses.next();
      Relationship rel = response.getRelationship();

      relations.add(RelationshipInfo.builder()
        .resourceType(rel.getResource().getObjectType())
        .resourceId(rel.getResource().getObjectId())
        .relation(rel.getRelation())
        .toResourceType(rel.getSubject().getObject().getObjectType())
        .toResourceId(rel.getSubject().getObject().getObjectId())
        .build());
    }

    return relations;
  }

  /**
   * Get all relationships where this resource is the target (has relations pointing IN)
   * Example: cohort:xyz has parent -> schema:abc (schema is the target)
   */
  public List<RelationshipInfo> getIncomingRelations(String resourceType, String resourceId,
                                                     String relationName) {
    ReadRelationshipsRequest request = ReadRelationshipsRequest.newBuilder()
      .setRelationshipFilter(RelationshipFilter.newBuilder()
        .setOptionalRelation(relationName)
        .setOptionalSubjectFilter(SubjectFilter.newBuilder()
          .setSubjectType(resourceType)
          .setOptionalSubjectId(resourceId)
          .build())
        .build())
      .build();

    List<RelationshipInfo> relations = new ArrayList<>();
    Iterator<ReadRelationshipsResponse> responses = permissionsClient.readRelationships(request);

    while (responses.hasNext()) {
      ReadRelationshipsResponse response = responses.next();
      Relationship rel = response.getRelationship();

      relations.add(RelationshipInfo.builder()
        .resourceType(rel.getResource().getObjectType())
        .resourceId(rel.getResource().getObjectId())
        .relation(rel.getRelation())
        .toResourceType(rel.getSubject().getObject().getObjectType())
        .toResourceId(rel.getSubject().getObject().getObjectId())
        .build());
    }

    return relations;
  }


  /**
   * Write relationship with sub-relation (for group membership)
   * This is what sets userset_relation = "member"
   */
  public void writeRelationshipWithSubRelation(String resourceType, String resourceId,
                                               String relation, String subjectType,
                                               String subjectId, String subRelation) {
    WriteRelationshipsRequest request = WriteRelationshipsRequest.newBuilder()
      .addUpdates(RelationshipUpdate.newBuilder()
        .setOperation(RelationshipUpdate.Operation.OPERATION_TOUCH)
        .setRelationship(Relationship.newBuilder()
          .setResource(ObjectReference.newBuilder()
            .setObjectType(resourceType)
            .setObjectId(resourceId)
            .build())
          .setRelation(relation)
          .setSubject(SubjectReference.newBuilder()
            .setObject(ObjectReference.newBuilder()
              .setObjectType(subjectType)
              .setObjectId(subjectId)
              .build())
            .setOptionalRelation(subRelation)  // ← This sets userset_relation!
            .build())
          .build())
        .build())
      .build();

    permissionsClient.writeRelationships(request);
  }

  /**
   * Delete relationship with sub-relation
   */
  public void deleteRelationshipWithSubRelation(String resourceType, String resourceId,
                                                String relation, String subjectType,
                                                String subjectId, String subRelation) {
    DeleteRelationshipsRequest request = DeleteRelationshipsRequest.newBuilder()
      .setRelationshipFilter(RelationshipFilter.newBuilder()
        .setResourceType(resourceType)
        .setOptionalResourceId(resourceId)
        .setOptionalRelation(relation)
        .setOptionalSubjectFilter(SubjectFilter.newBuilder()
          .setSubjectType(subjectType)
          .setOptionalSubjectId(subjectId)
          .build())
        .build())
      .build();

    permissionsClient.deleteRelationships(request);
  }

}

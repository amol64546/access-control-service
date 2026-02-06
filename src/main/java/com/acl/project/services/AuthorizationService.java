package com.acl.project.services;

import com.acl.project.dto.ConditionalPermissionRequest;
import com.acl.project.dto.RelationshipInfo;
import com.acl.project.enums.Permission;
import com.acl.project.enums.Relation;
import com.acl.project.enums.Resource;
import com.acl.project.enums.Subject;
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

  public void writeRelationship(Resource resource, String resourceId,
                                Relation relation, String subject, String subjectId) {
    WriteRelationshipsRequest request = WriteRelationshipsRequest.newBuilder()
      .addUpdates(RelationshipUpdate.newBuilder()
        .setOperation(RelationshipUpdate.Operation.OPERATION_TOUCH)
        .setRelationship(Relationship.newBuilder()
          .setResource(ObjectReference.newBuilder()
            .setObjectType(resource.name().toLowerCase())
            .setObjectId(resourceId)
            .build())
          .setRelation(relation.name().toLowerCase())
          .setSubject(SubjectReference.newBuilder()
            .setObject(ObjectReference.newBuilder()
              .setObjectType(subject.toLowerCase())
              .setObjectId(subjectId)
              .build())
            .build())
          .build())
        .build())
      .build();

    permissionsClient.writeRelationships(request);
  }


  public void writeRelationship(
    Resource resourceType,
    String resourceId,
    Relation relation,
    Subject subject,
    String subjectId,
    ConditionalPermissionRequest conditionalPermissionRequest
  ) {

    // Build caveat context (Struct)
    Struct.Builder contextBuilder = Struct.newBuilder();
    if (StringUtils.isNotBlank(conditionalPermissionRequest.getPassword())) {
      contextBuilder.putFields(CAVEAT_KEY, Value.newBuilder().setStringValue(conditionalPermissionRequest.getPassword()).build());
    }

    Relationship relationship =
      Relationship.newBuilder()
        .setResource(
          ObjectReference.newBuilder()
            .setObjectType(resourceType.name().toLowerCase())
            .setObjectId(resourceId)
            .build())
        .setRelation(relation.name().toLowerCase())
        .setSubject(
          SubjectReference.newBuilder()
            .setObject(
              ObjectReference.newBuilder()
                .setObjectType(subject.name().toLowerCase())
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


  public boolean checkPermission(Resource resource, String resourceId,
                                 Permission permission, Subject requesterType, String requesterId) {
    CheckPermissionRequest request = CheckPermissionRequest.newBuilder()
      .setResource(ObjectReference.newBuilder()
        .setObjectType(resource.name().toLowerCase())
        .setObjectId(resourceId)
        .build())
      .setPermission(permission.name().toLowerCase())
      .setSubject(SubjectReference.newBuilder()
        .setObject(ObjectReference.newBuilder()
          .setObjectType(requesterType.name().toLowerCase())
          .setObjectId(requesterId)
          .build())
        .build())
      .build();

    CheckPermissionResponse response = permissionsClient.checkPermission(request);
    return response.getPermissionship() ==
      CheckPermissionResponse.Permissionship.PERMISSIONSHIP_HAS_PERMISSION;
  }

  public boolean checkPermission(
    Resource resourceType,
    String resourceId,
    Permission permission,
    Subject requesterType,
    String requesterId,
    ConditionalPermissionRequest conditionalPermissionRequest
  ) {

    Struct.Builder contextBuilder = Struct.newBuilder();

    if (StringUtils.isNotBlank(conditionalPermissionRequest.getPassword())) {
      contextBuilder.putFields(
        CAVEAT_SUPPLIED_KEY,
        Value.newBuilder().setStringValue(conditionalPermissionRequest.getPassword()).build()
      );
    }

    CheckPermissionRequest request =
      CheckPermissionRequest.newBuilder()
        .setResource(
          ObjectReference.newBuilder()
            .setObjectType(resourceType.name().toLowerCase())
            .setObjectId(resourceId)
            .build())
        .setPermission(permission.name().toLowerCase())
        .setSubject(
          SubjectReference.newBuilder()
            .setObject(
              ObjectReference.newBuilder()
                .setObjectType(requesterType.name().toLowerCase())
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


  public void deleteRelationship(Resource resource, String resourceId) {
    DeleteRelationshipsRequest request =
      DeleteRelationshipsRequest.newBuilder()
        .setRelationshipFilter(
          RelationshipFilter.newBuilder()
            .setResourceType(resource.name().toLowerCase())
            .setOptionalResourceId(resourceId)
            .build()
        )
        .build();

    permissionsClient.deleteRelationships(request);
  }

  public void deleteRelationship(Resource resource, String resourceId, Relation relation,
                                 Subject subject, String subjectId) {
    DeleteRelationshipsRequest request = DeleteRelationshipsRequest.newBuilder()
      .setRelationshipFilter(RelationshipFilter.newBuilder()
        .setResourceType(resource.name().toLowerCase())
        .setOptionalResourceId(resourceId)
        .setOptionalRelation(relation.name().toLowerCase())
        .setOptionalSubjectFilter(SubjectFilter.newBuilder()
          .setSubjectType(subject.name().toLowerCase())
          .setOptionalSubjectId(subjectId)
          .build())
        .build())
      .build();

    permissionsClient.deleteRelationships(request);
  }


  public List<RelationshipInfo> getOutgoingRelations(Resource resource, String resourceId,
                                                     Relation relation) {
    ReadRelationshipsRequest request = ReadRelationshipsRequest.newBuilder()
      .setRelationshipFilter(RelationshipFilter.newBuilder()
        .setResourceType(resource.name().toLowerCase())
        .setOptionalResourceId(resourceId)
        .setOptionalRelation(relation.name().toLowerCase())
        .build())
      .build();

    List<RelationshipInfo> relations = new ArrayList<>();
    Iterator<ReadRelationshipsResponse> responses = permissionsClient.readRelationships(request);

    while (responses.hasNext()) {
      ReadRelationshipsResponse response = responses.next();
      Relationship rel = response.getRelationship();

      relations.add(RelationshipInfo.builder()
        .resource(Resource.valueOf(rel.getResource().getObjectType().toUpperCase()))
        .resourceId(rel.getResource().getObjectId())
        .relation(Relation.valueOf(rel.getRelation().toUpperCase()))
        .toResource(Resource.valueOf(rel.getSubject().getObject().getObjectType().toUpperCase()))
        .toResourceId(rel.getSubject().getObject().getObjectId())
        .build());
    }

    return relations;
  }

  /**
   * Get all relationships where this resource is the target (has relations pointing IN)
   * Example: cohort:xyz has parent -> schema:abc (schema is the target)
   */
  public List<RelationshipInfo> getIncomingRelations(Resource resource, String resourceId,
                                                     Relation relation) {
    ReadRelationshipsRequest request = ReadRelationshipsRequest.newBuilder()
      .setRelationshipFilter(RelationshipFilter.newBuilder()
        .setOptionalRelation(relation.name().toLowerCase())
        .setOptionalSubjectFilter(SubjectFilter.newBuilder()
          .setSubjectType(resource.name().toLowerCase())
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
        .resource(Resource.valueOf(rel.getResource().getObjectType().toUpperCase()))
        .resourceId(rel.getResource().getObjectId())
        .relation(Relation.valueOf(rel.getRelation().toUpperCase()))
        .toResource(Resource.valueOf(rel.getSubject().getObject().getObjectType().toUpperCase()))
        .toResourceId(rel.getSubject().getObject().getObjectId())
        .build());
    }

    return relations;
  }


  /**
   * Write relationship with sub-relation (for group membership)
   * This is what sets userset_relation = "member"
   */
  public void writeRelationshipWithSubRelation(Resource resource, String resourceId,
                                               Relation relation, Subject subject,
                                               String subjectId, Relation subRelation) {
    WriteRelationshipsRequest request = WriteRelationshipsRequest.newBuilder()
      .addUpdates(RelationshipUpdate.newBuilder()
        .setOperation(RelationshipUpdate.Operation.OPERATION_TOUCH)
        .setRelationship(Relationship.newBuilder()
          .setResource(ObjectReference.newBuilder()
            .setObjectType(resource.name().toLowerCase())
            .setObjectId(resourceId)
            .build())
          .setRelation(relation.name().toLowerCase())
          .setSubject(SubjectReference.newBuilder()
            .setObject(ObjectReference.newBuilder()
              .setObjectType(subject.name().toLowerCase())
              .setObjectId(subjectId)
              .build())
            .setOptionalRelation(subRelation.name().toLowerCase()) // ← This sets userset_relation!
            .build())
          .build())
        .build())
      .build();

    permissionsClient.writeRelationships(request);
  }

  /**
   * Delete relationship with sub-relation
   */
  public void deleteRelationshipWithSubRelation(Resource resource, String resourceId,
                                                Relation relation, Subject subject,
                                                String subjectId, String subRelation) {
    DeleteRelationshipsRequest request = DeleteRelationshipsRequest.newBuilder()
      .setRelationshipFilter(RelationshipFilter.newBuilder()
        .setResourceType(resource.name().toLowerCase())
        .setOptionalResourceId(resourceId)
        .setOptionalRelation(relation.name().toLowerCase())
        .setOptionalSubjectFilter(SubjectFilter.newBuilder()
          .setSubjectType(subject.name().toLowerCase())
          .setOptionalSubjectId(subjectId)
          .build())
        .build())
      .build();

    permissionsClient.deleteRelationships(request);
  }

}

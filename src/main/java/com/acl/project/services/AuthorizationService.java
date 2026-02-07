package com.acl.project.services;

import com.acl.project.builders.PermissionOptions;
import com.acl.project.builders.RelationshipOptions;
import com.acl.project.dto.RelationshipInfo;
import com.acl.project.enums.Relation;
import com.acl.project.enums.Resource;
import com.acl.project.enums.Subject;
import com.authzed.api.v1.*;
import com.google.protobuf.Struct;
import com.google.protobuf.Timestamp;
import com.google.protobuf.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.acl.project.utils.constants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {

  private final PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsClient;

  public void writeRelationship(RelationshipOptions options) {
    SubjectReference.Builder subjectBuilder =
      SubjectReference.newBuilder()
        .setObject(ObjectReference.newBuilder()
          .setObjectType(options.getSubject().name().toLowerCase())
          .setObjectId(options.getSubjectId())
          .build());

    // Optional sub relation, sets userset_relation
    if (options.getSubRelation() != null) {
      subjectBuilder.setOptionalRelation(
        options.getSubRelation().name().toLowerCase()
      );
    }

    Relationship.Builder relationshipBuilder =
      Relationship.newBuilder()
        .setResource(ObjectReference.newBuilder()
          .setObjectType(options.getResource().name().toLowerCase())
          .setObjectId(options.getResourceId())
          .build())
        .setRelation(options.getRelation().name().toLowerCase())
        .setSubject(subjectBuilder.build());


    // Add caveat if password is provided
    if (options.getPassword() != null) {
      Struct.Builder contextBuilder = Struct.newBuilder();
      contextBuilder.putFields(CAVEAT_KEY,
        Value.newBuilder().setStringValue(options.getPassword()).build());

      relationshipBuilder.setOptionalCaveat(
        ContextualizedCaveat.newBuilder()
          .setCaveatName(CAVEAT_NAME)
          .setContext(contextBuilder.build())
          .build());
    }

    // Add expiration if provided
//    if (options.getDaysFromNow() != null) {
//      Timestamp expiration = getTimestamp(options);
//      relationshipBuilder.setOptionalExpiresAt(expiration);
//    }

    WriteRelationshipsRequest request = WriteRelationshipsRequest.newBuilder()
      .addUpdates(RelationshipUpdate.newBuilder()
        .setOperation(RelationshipUpdate.Operation.OPERATION_TOUCH)
        .setRelationship(relationshipBuilder.build())
        .build())
      .build();

    permissionsClient.writeRelationships(request);

  }

  /**
   * Unified check permission method with optional caveat context
   */
  public boolean checkPermission(PermissionOptions options) {
    CheckPermissionRequest.Builder requestBuilder = CheckPermissionRequest.newBuilder()
      .setResource(ObjectReference.newBuilder()
        .setObjectType(options.getResource().name().toLowerCase())
        .setObjectId(options.getResourceId())
        .build())
      .setPermission(options.getPermission().name().toLowerCase())
      .setSubject(SubjectReference.newBuilder()
        .setObject(ObjectReference.newBuilder()
          .setObjectType(options.getSubject().name().toLowerCase())
          .setObjectId(options.getSubjectId())
          .build())
        .build());

    // Add context if password is provided
    if (options.getPassword() != null) {
      Struct.Builder contextBuilder = Struct.newBuilder();
      contextBuilder.putFields(CAVEAT_SUPPLIED_KEY,
        Value.newBuilder().setStringValue(options.getPassword()).build());
      requestBuilder.setContext(contextBuilder.build());
    }

    CheckPermissionResponse response = permissionsClient.checkPermission(requestBuilder.build());
    return response.getPermissionship() ==
      CheckPermissionResponse.Permissionship.PERMISSIONSHIP_HAS_PERMISSION;
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


//  private static Timestamp getTimestamp(RelationshipOptions options) {
//    Instant expirationTime = Instant.now().plus(options.getDaysFromNow(), ChronoUnit.DAYS);
//    return Timestamp.newBuilder()
//      .setSeconds(expirationTime.getEpochSecond())
//      .setNanos(expirationTime.getNano())
//      .build();
//  }

}

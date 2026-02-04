package com.acl.project.services;

import com.authzed.api.v1.SchemaServiceGrpc;
import com.authzed.api.v1.WriteSchemaRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.acl.project.utils.constants.SPICEDB_SCHEMA_FILE_NAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchemaService {

  private final SchemaServiceGrpc.SchemaServiceBlockingStub schemaClient;

  @PostConstruct
  public void writeSchema() {
    try {
      log.info("Initializing SpiceDB schema...");
      String schema = loadSchemaFromResource();

      WriteSchemaRequest request = WriteSchemaRequest.newBuilder()
        .setSchema(schema)
        .build();

      schemaClient.writeSchema(request);
      log.info("SpiceDB schema written successfully");

    } catch (Exception e) {
      log.error("Failed to write SpiceDB schema", e);
      throw new RuntimeException("Failed to initialize SpiceDB schema", e);
    }
  }

  private String loadSchemaFromResource() throws IOException {
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(SPICEDB_SCHEMA_FILE_NAME)) {
      if (inputStream == null) {
        throw new IOException("Schema file not found in resources.");
      }
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
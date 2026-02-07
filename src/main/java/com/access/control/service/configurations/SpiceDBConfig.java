package com.access.control.service.configurations;

import com.authzed.api.v1.PermissionsServiceGrpc;
import com.authzed.api.v1.SchemaServiceGrpc;
import com.authzed.grpcutil.BearerToken;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpiceDBConfig {

  @Value("${spicedb.host:localhost}")
  private String spicedbHost;

  @Value("${spicedb.port:50051}")
  private int spicedbPort;

  @Value("${spicedb.token}")
  private String spicedbToken;

  @Bean
  public ManagedChannel spicedbChannel() {
    return ManagedChannelBuilder
      .forAddress(spicedbHost, spicedbPort)
      .usePlaintext()
      .build();
  }

  @Bean
  public PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsClient(ManagedChannel channel) {
    return PermissionsServiceGrpc.newBlockingStub(channel)
      .withCallCredentials(new BearerToken(spicedbToken));
  }

  @Bean
  public SchemaServiceGrpc.SchemaServiceBlockingStub schemaClient(ManagedChannel channel) {
    return SchemaServiceGrpc.newBlockingStub(channel)
      .withCallCredentials(new BearerToken(spicedbToken));
  }
}
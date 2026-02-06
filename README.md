# Access Control Service (ACL Project)

A Spring Boot-based role and attribute-based access control (RBAC/ABAC) service powered by **SpiceDB**. This service provides comprehensive permission management, resource hierarchy, and group-based access control for multi-tenant applications.

## 📋 Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
  - [Installation](#installation)
  - [Configuration](#configuration)
  - [Running the Application](#running-the-application)
- [API Overview](#api-overview)
  - [Resources](#resources)
  - [Groups](#groups)
- [Core Concepts](#core-concepts)
  - [Tenants](#tenants)
  - [Groups](#groups-1)
  - [Resources](#resources-1)
  - [Permissions](#permissions)
  - [Hierarchy](#hierarchy)
- [Schema Definition](#schema-definition)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Configuration](#configuration-1)

## ✨ Features

- **Fine-Grained Access Control**: Define and enforce permissions at multiple levels with SpiceDB
- **Group Management**: Create and manage groups with admins and members
- **Hierarchical Resources**: Support for nested resource hierarchies (tenants → dataverse → schema → cohort)
- **Permission Models**: 
  - Owner-based permissions
  - Editor/Viewer access levels
  - Conditional permissions with platform and app matching
  - Permission inheritance through hierarchy
- **Multi-Tenant Support**: Isolated access control per tenant
- **REST API**: Comprehensive endpoints for resource and group management
- **Permission Checking**: Real-time permission validation

## 🏗️ Architecture

This service implements a fine-grained authorization system using **SpiceDB**, an open-source authorization system that uses a schema-based approach to define relationships and permissions.

```
┌─────────────────────────────────────────┐
│     REST API Controllers                │
├─────────────────────────────────────────┤
│  ├─ ResourceController                  │
│  └─ GroupController                     │
├─────────────────────────────────────────┤
│     Service Layer                       │
├─────────────────────────────────────────┤
│  ├─ ResourceService                     │
│  ├─ GroupService                        │
│  ├─ AuthorizationService               │
│  ├─ HierarchyService                   │
│  └─ SchemaService                      │
├─────────────────────────────────────────┤
│     SpiceDB Integration                 │
├─────────────────────────────────────────┤
│  SpiceDB (Authorization Backend)        │
└─────────────────────────────────────────┘
```

## 📦 Prerequisites

- **Java 21** or higher
- **Maven 3.6+** (or use the included `mvnw`/`mvnw.cmd`)
- **SpiceDB Instance** (running and accessible)
- **Spring Boot 3.5.10**

## 🚀 Getting Started

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd access-control-service
   ```

2. **Build the project**
   ```bash
   ./mvnw clean install
   ```
   Or on Windows:
   ```bash
   mvnw.cmd clean install
   ```

### Configuration

Configure the application by editing `src/main/resources/application.properties`:

```properties
# Application name
spring.application.name=acl-project

# SpiceDB Configuration
spicedb.token=root
# Add the following properties as needed:
# spicedb.host=localhost
# spicedb.port=50051
# spicedb.endpoint=http://localhost:8443
```

### Running the Application

1. **Start SpiceDB** (if not already running)
   ```bash
   # Ensure SpiceDB is accessible at the configured endpoint
   ```

2. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```
   Or on Windows:
   ```bash
   mvnw.cmd spring-boot:run
   ```

   The application will start on `http://localhost:8080` by default.

## 📡 API Overview

### Resources

#### Create Resource
```
POST /api/resources
Content-Type: application/json

{
  "resourceId": "resource-123",
  "resource": "SCHEMA",
  "ownerId": "user-1",
  "parentResourceId": "dataverse-456"
}
```

#### Check Permission
```
GET /api/resources
Content-Type: application/json

{
  "resourceId": "resource-123",
  "userId": "user-1",
  "permission": "read"
}
```

#### Grant Permission
```
POST /api/resources/grant
Content-Type: application/json

{
  "resourceId": "resource-123",
  "principalId": "user-1",
  "permission": "write",
  "relationType": "EDITOR"
}
```

#### Revoke Permission
```
DELETE /api/resources/revoke
Content-Type: application/json

{
  "resourceId": "resource-123",
  "principalId": "user-1",
  "permission": "write",
  "relationType": "EDITOR"
}
```

#### Delete Resource
```
DELETE /api/resources
Content-Type: application/json

{
  "resourceId": "resource-123"
}
```

### Groups

#### Create Group
```
POST /api/groups
Content-Type: application/json

{
  "groupId": "group-1",
  "ownerId": "user-1"
}
```

#### Add Group Admin
```
POST /api/groups/{groupId}/admins
Content-Type: application/json

{
  "adminId": "user-2",
  "requesterId": "user-1"
}
```

#### Remove Group Admin
```
DELETE /api/groups/{groupId}/admins/{adminId}?requesterId={requesterId}
```

#### Add Group Member
```
POST /api/groups/{groupId}/members
Content-Type: application/json

{
  "memberId": "user-3",
  "requesterId": "user-1"
}
```

#### Remove Group Member
```
DELETE /api/groups/{groupId}/members/{memberId}?requesterId={requesterId}
```

## 💡 Core Concepts

### Tenants
A tenant represents the top-level organizational unit. Each tenant has isolated resources and relationships.

### Groups
Groups organize users and can be assigned to resources as editors or viewers. Each group has:
- **Owner**: Full control over the group
- **Admins**: Can manage members (with permission from owner)
- **Members**: Regular members who can be assigned permissions

### Resources
Resources represent data objects in the system. Supported resource types:
- **TENANT**: Top-level organizational unit
- **DATAVERSE**: Logical grouping of schemas
- **SCHEMA**: Data schema definition
- **COHORT**: Subset of data in a schema

### Permissions
Permissions define what actions can be performed on resources:
- **read**: View resource and its data
- **write**: Modify resource content
- **delete**: Remove resource
- **grant**: Grant permissions to others
- **revoke**: Revoke permissions from others
- **manage_members**: Manage group members (for groups only)
- **delete_group**: Delete a group (for groups only)

### Hierarchy
Resources support hierarchical relationships:
```
Tenant
  └─ Dataverse
      └─ Schema
          └─ Cohort
```

Permissions are inherited down the hierarchy. If a user can write to a Dataverse, they can also write to all nested Schemas and Cohorts.

## 📄 Schema Definition

The SpiceDB schema is defined in `src/main/resources/construct_schema.zed`. Key elements:

**Tenant Definition**
```zed
definition tenant {}
```

**Group Definition**
```zed
definition group {
    relation owner: tenant
    relation admin: tenant
    relation member: tenant

    permission manage_members = owner + admin
    permission delete_group = owner
}
```

**Resource Definitions** (Dataverse, Schema, Cohort)
```zed
definition dataverse {
    relation owner: tenant
    relation editor: tenant | tenant with platform_app_match | group#member
    relation viewer: tenant | tenant with platform_app_match | group#member

    permission delete = owner
    permission write  = owner + editor
    permission read   = viewer + write
    permission grant  = owner
    permission revoke = owner
}
```

**Caveats**
```zed
caveat platform_app_match(platformId string, appId string, supplied_platformId string, supplied_appId string) {
    platformId == supplied_platformId && appId == supplied_appId
}
```

Caveats allow conditional permission evaluation based on runtime parameters.

## 🛠️ Technology Stack

| Component | Technology |
|-----------|-----------|
| **Framework** | Spring Boot 3.5.10 |
| **Language** | Java 21 |
| **Build Tool** | Maven |
| **Authorization Backend** | SpiceDB 1.5.4 |
| **RPC Framework** | gRPC 1.72.0 |
| **Dependency Injection** | Spring DI |
| **Utilities** | Lombok, Apache Commons Lang3 |

## 📁 Project Structure

```
access-control-service/
├── src/
│   ├── main/
│   │   ├── java/com/acl/project/
│   │   │   ├── AclProjectApplication.java          # Application entry point
│   │   │   ├── configurations/
│   │   │   │   └── SpiceDBConfig.java              # SpiceDB configuration
│   │   │   ├── controllers/
│   │   │   │   ├── GroupController.java            # Group endpoints
│   │   │   │   └── ResourceController.java         # Resource endpoints
│   │   │   ├── services/
│   │   │   │   ├── AuthorizationService.java       # Core authorization logic
│   │   │   │   ├── ResourceService.java            # Resource management
│   │   │   │   ├── GroupService.java               # Group management
│   │   │   │   ├── HierarchyService.java           # Hierarchy operations
│   │   │   │   └── SchemaService.java              # Schema management
│   │   │   ├── dto/                                # Data Transfer Objects
│   │   │   ├── enums/                              # Enumerations
│   │   │   ├── exception/                          # Exception handling
│   │   │   └── utils/                              # Utility classes
│   │   ├── resources/
│   │   │   ├── application.properties              # Application configuration
│   │   │   └── construct_schema.zed                # SpiceDB schema
│   └── test/                                       # Test files
├── pom.xml                                         # Maven configuration
├── mvnw & mvnw.cmd                                 # Maven wrapper scripts
└── README.md                                       # This file
```

## 🔧 Configuration

### Spring Boot Properties

Edit `src/main/resources/application.properties`:

```properties
# Application Configuration
spring.application.name=acl-project
server.port=8080
server.servlet.context-path=/

# Logging
logging.level.root=INFO
logging.level.com.acl.project=DEBUG

# SpiceDB Configuration
spicedb.token=root
# Optional: Configure SpiceDB connection details
# spicedb.host=localhost
# spicedb.port=50051
# spicedb.endpoint=http://localhost:8443
```

## 📝 API Documentation

For detailed API documentation, import the included Postman collection:
- **File**: `src/main/resources/acl-project.postman_collection.json`
- **Import into Postman** and explore all endpoints with pre-configured examples

## 🤝 Contributing

1. Create a feature branch (`git checkout -b feature/AmazingFeature`)
2. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
3. Push to the branch (`git push origin feature/AmazingFeature`)
4. Open a Pull Request

## 📜 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📧 Support

For issues, questions, or suggestions, please open an issue on the repository.

---

**Last Updated**: February 2026  
**Version**: 0.0.1-SNAPSHOT

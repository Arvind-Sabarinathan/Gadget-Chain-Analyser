# Gadget Chain Analyser (GCA)

[![Java 21](https://img.shields.io/badge/Java-21-ed8b00?style=flat-square&logo=java&logoColor=white)](https://adoptium.net/)
[![Spring Boot 3.5.7](https://img.shields.io/badge/Spring_Boot-3.5.7-6db33f?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-47a248?style=flat-square&logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![ASM 9.6](https://img.shields.io/badge/ASM-9.6-007ec6?style=flat-square)](https://asm.ow2.io/)

## About

Gadget Chain Analyser is a Spring Boot web application that performs static bytecode analysis on uploaded JAR files to detect potential Java deserialization gadget chains and security vulnerabilities. It uses the [ASM](https://asm.ow2.io/) framework to inspect every `.class` file inside an archive, identifies suspicious serialization methods, risky third-party packages, and known security sinks, and surfaces the findings through a responsive dark-mode dashboard.

## Features

- **In-Memory JAR Analysis** -- Upload JAR files (up to 50 MB) processed entirely in memory; no permanent disk storage.
- **Bytecode-Level Scanning** -- ASM-based ClassVisitor inspects every class entry for:
  - **Suspicious methods** -- Serialization hook methods (`readObject`, `readResolve`, `readExternal`, `writeObject`, `writeReplace`, `finalize`, `clone`, `compareTo`)
  - **Risky packages** -- Known deserialization-prone libraries (Commons Collections, Spring, Groovy, XStream, Javassist, etc.)
  - **Security sinks** -- Dangerous method invocations (`Runtime.exec`, `ProcessBuilder.start`, `Method.invoke`, etc.)
- **Three-Tier Severity Model**:
  | Severity | Criteria |
  |---|---|
  | **HIGH** | Deserialization entry points (`readObject`, `readResolve`, `readExternal`) or security sinks |
  | **MEDIUM** | Write-side methods (`writeObject`, `writeReplace`) or classes from risky packages |
  | **LOW** | Other suspicious methods (`finalize`, `clone`, `compareTo`) or parsing errors |
- **Parallel Class Scanning** -- Each `.class` file is analyzed concurrently using a CPU-core-sized thread pool for faster analysis of large JARs.
- **MongoDB Persistence** -- Analysis results and metadata are stored for historical review and trend tracking.
- **Web Dashboard** -- Thymeleaf + Tailwind CSS UI with:
  - Upload page with live loading modal
  - Results page with severity summary cards (HIGH / MEDIUM / LOW / TOTAL) and expandable per-finding details
  - Artifact history with search, status filtering, sorting, and pagination
  - Dark theme with responsive layout
- **REST API** -- Programmatic access to upload, list, and retrieve analysis results.

## Quick Start

### Prerequisites

- Java 21 (JDK)
- MongoDB (running on `localhost:27017`)
- Maven (or use the included `mvnw` wrapper)

### Build and Run

```bash
# Clone the repository
git clone https://github.com/your-username/gadget-chain-analyser.git
cd gadget-chain-analyser

# Build the project
./mvnw clean package

# Run the application
java -jar target/gca-0.0.1-SNAPSHOT.jar
# or
./mvnw spring-boot:run
```

The application will be available at **http://localhost:8080**.

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/artifacts/upload` | Upload a JAR file for analysis (multipart form with `file` field) |
| `GET`  | `/api/artifacts` | List all analyzed artifacts |
| `GET`  | `/api/artifacts/{id}` | Retrieve a specific analysis result by ID |

## Detection Details

### Suspicious Methods (Serialization Hooks)

Java serialization allows classes to define special methods that are called during serialization or deserialization. These are common entry points for gadget chains:

| Method | Role |
|--------|------|
| `readObject` | Custom deserialization logic |
| `readResolve` | Object replacement after deserialization |
| `readExternal` | Externalizable deserialization |
| `writeObject` | Custom serialization logic |
| `writeReplace` | Object replacement before serialization |
| `finalize` | Cleanup before GC (can be triggered by serialization) |
| `clone` | Object copying |
| `compareTo` | Ordering (used in sorting-based gadgets) |

### Risky Packages

Classes from these packages are flagged as medium severity:

`org/apache/commons/collections`, `org/apache/commons/beanutils`, `org/springframework`, `org/codehaus/groovy`, `com/mchange`, `org/python`, `org/apache/commons/logging`, `com/sun/rowset`, `com/thoughtworks/xstream`, `javassist`

### Security Sinks

Method invocations that can lead to code execution or reflection-based attacks:

- `java/lang/Runtime.exec`
- `java/lang/ProcessBuilder.start`
- `java/lang/reflect/Method.invoke`
- `java/lang/reflect/Constructor.newInstance`
- `com/sun/org/apache/xalan/internal/xsltc/trax/TemplatesImpl.newTransformer`

## Configuration

Key properties in `src/main/resources/application.properties`:

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8080` | HTTP server port |
| `spring.data.mongodb.uri` | `mongodb://localhost:27017/gca` | MongoDB connection string |
| `spring.servlet.multipart.max-file-size` | `50MB` | Maximum uploaded file size |
| `logging.level.org.springframework.web` | `INFO` | Web request logging level |

## Tech Stack

| Technology | Purpose |
|---|---|
| **Java 21** | Language and runtime |
| **Spring Boot 3.5.7** | Application framework (IoC, MVC, REST) |
| **Spring Data MongoDB** | NoSQL database persistence |
| **MongoDB** | Document store |
| **ASM 9.6** | Bytecode manipulation and class file analysis |
| **Thymeleaf** | Server-side HTML templating |
| **Tailwind CSS** | Utility-first CSS framework (via CDN) |
| **Font Awesome 6** | Icon library (via CDN) |
| **Maven** | Build and dependency management |

## Project Structure

```
src/
+-- main/
|   +-- java/com/argus/gca/
|   |   +-- GcaApplication.java          # Spring Boot entry point
|   |   +-- controller/
|   |   |   +-- ArtifactController.java  # REST API (/api/artifacts)
|   |   |   +-- MainController.java      # MVC page routes
|   |   +-- model/
|   |   |   +-- Artifact.java            # MongoDB document model
|   |   +-- repository/
|   |   |   +-- ArtifactRepository.java  # Spring Data MongoDB repository
|   |   +-- service/
|   |       +-- AnalyserService.java     # Core ASM-based bytecode analyser
|   |       +-- ArtifactService.java     # Upload and analysis orchestration
|   +-- resources/
|       +-- application.properties       # Server and database configuration
|       +-- templates/                   # Thymeleaf views
|           +-- layout.html              # Master layout (nav, footer)
|           +-- index.html               # Home page
|           +-- upload.html              # Upload form with loading modal
|           +-- artifact.html            # Single analysis results
|           +-- artifacts.html           # Artifact history listing
|           +-- result.html              # Analysis result display
```

# ERP Lite - Estructura de Módulos Maven

Proyecto modular con arquitectura por capas basada en Maven.

## Estructura de Módulos

```
erp-lite/
├── pom.xml (padre - configura todos los módulos)
├── erp-common/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/erp/common/
│       └── test/java/com/erp/common/
├── erp-domain/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/erp/domain/
│       └── test/java/com/erp/domain/
├── erp-application/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/erp/application/
│       └── test/java/com/erp/application/
├── erp-infrastructure/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/erp/infrastructure/
│       └── test/java/com/erp/infrastructure/
└── erp-api/
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/erp/api/
        │   └── resources/application.yaml
        └── test/java/com/erp/api/
```

## Descripción de Módulos

- **erp-common**: Utilidades comunes y helpers reutilizables
- **erp-domain**: Entidades y modelos de negocio (JPA)
- **erp-application**: Servicios de aplicación y casos de uso
- **erp-infrastructure**: Implementación de persistencia y repositorios
- **erp-api**: REST API con Spring Web (módulo ejecutable)

## Dependencias

### Transversales
- **Lombok**: En todos los módulos (versión 1.18.30)

### Por Módulo
- **erp-common**: Spring Context
- **erp-domain**: Spring Data JPA
- **erp-application**: Spring Context
- **erp-infrastructure**: Spring Data JPA, H2 Database
- **erp-api**: Spring Web, Spring Boot (ejecutable)

## Compilar el Proyecto

```bash
# En la raíz del proyecto (erp-lite)
mvn clean install

# O compilar solo el módulo API
mvn clean package -pl erp-api -am
```

## Ejecutar la Aplicación

```bash
cd erp-lite/erp-api
mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080/api`

## Punto de Entrada

La clase principal se encuentra en:
`erp-api/src/main/java/com/erp/api/ErpApiApplication.java`

## Health Check

```bash
curl http://localhost:8080/api/health
```

Respuesta esperada:
```
ERP Lite API is running!
```

## Notas

- Java 11+
- Spring Boot 3.1.5
- Maven 3.6+
- La arquitectura permite escalabilidad y separación de responsabilidades
- Cada módulo es independiente y reutilizable

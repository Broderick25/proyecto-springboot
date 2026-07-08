spring:
  application:
    name: erp-lite-api

  # PostgreSQL
  datasource:
    url: jdbc:postgresql://localhost:5433/erp_db
    username: debuggeandoideas
    password: secret
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5

  # JPA / Hibernate
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: ${JPA_SHOW_SQL:true}
    open-in-view: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: ${HIBERNATE_FORMAT_SQL:true}
        use_sql_comments: ${HIBERNATE_SHOW_COMMENTS:true}

  # MongoDB - Spring Boot 4.x
  data:
    mongodb:
      auto-index-creation: true

    # Redis
    redis:
      host: localhost
      port: 6379
      password: secret
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0

  mongodb:
    uri: mongodb://debuggeandoideas:secret@localhost:27017/erp_catalog_db?authSource=admin

# Server
server:
  port: 9090
  servlet:
    context-path: /api

# Logging
logging:
  level:
    root: ${LOG_LEVEL_ROOT:INFO}
    com.erp: ${LOG_LEVEL_APP:DEBUG}
    org.springframework.web: ${LOG_LEVEL_WEB:DEBUG}
    org.springframework.data: ${LOG_LEVEL_DATA:DEBUG}
    org.hibernate.SQL: ${LOG_LEVEL_SQL:DEBUG}
    org.hibernate.type.descriptor.sql.BasicBinder: ${LOG_LEVEL_SQL_PARAMS:TRACE}
    org.mongodb.driver: ${LOG_LEVEL_MONGO:DEBUG}

---
# Perfil "dev": expone detalles de error solo en desarrollo local, nunca en prod
spring:
  config:
    activate:
      on-profile: dev
  web:
    error:
      include-message: ${ERROR_INCLUDE_MESSAGE:always}
      include-binding-errors: ${ERROR_INCLUDE_BINDING:always}
      include-stacktrace: ${ERROR_INCLUDE_STACKTRACE:always}
# Pendientea

- Activar el perfil `local` al ejecutar la app para que tome `application-local.yaml` (con las credenciales de Gmail). Opciones:
  - Variable de entorno: `SPRING_PROFILES_ACTIVE=local`
  - Argumento al arrancar: `--spring.profiles.active=local`
  - En el IDE (IntelliJ/VS Code): agregar `local` en "Active profiles" de la configuración de ejecución

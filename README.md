# Administración de flotas de transporte saas

Backend para la implementación de un sistema de administración de flotas

el objetivo de este sistema es

+ Tener el inventario de unidades
+ Llevar el control de las llnatas asi como su desgaste
+ Llevar la administración de sus gastos y lógistica

## PAsos para correr el proyecto

1. Copia el env.example a un .env y llenalo con los datos correctos.

2. Corre el docker compose, ya que esta es la BD local
```bash
docker compose up -d
```

3. Levanta el proyecto

```bash
./gradlew bootRun
```

4. Abre el navegador para acceder a swagger que es la documentación del proyecto

http://localhost:8080/swagger-ui/index.html#/


PRueba con tus claves que pusiste en el .env

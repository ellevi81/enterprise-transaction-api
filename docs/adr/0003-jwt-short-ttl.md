# [cite_start]ADR 003: JWT con expiración de 15 minutos 

## Contexto
[cite_start]Minimizar el riesgo de secuestro de sesión en la API.

## Decisión
[cite_start]Implementar tokens de acceso de corta duración (TTL 15 min).

## Justificación
[cite_start]En caso de robo de token, la ventana de ataque es mínima, forzando una re-validación constante por diseño de seguridad.

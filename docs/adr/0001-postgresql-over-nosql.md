# [cite_start]ADR 001: PostgreSQL por integridad referencial 

## Contexto
[cite_start]El sistema maneja transacciones financieras críticas donde un dato huérfano es inaceptable.

## Decisión
[cite_start]Usar PostgreSQL con esquema relacional estricto y cumplimiento ACID[cite: 237].

## Justificación
[cite_start]A diferencia de NoSQL, PostgreSQL garantiza que cada transacción se complete totalmente o falle sin dejar basura, asegurando la trazabilidad financiera.

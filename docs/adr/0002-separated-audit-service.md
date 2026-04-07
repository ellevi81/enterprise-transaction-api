# [cite_start]ADR 002: Separación física del servicio de auditoría 

## Contexto
[cite_start]Alineación con el estándar ISO 27001 que posee Basetis.

## Decisión
[cite_start]El log de auditoría corre en una base de datos independiente y aislada[cite: 237].

## Justificación
[cite_start]Si el servicio de transacciones es comprometido, el registro de auditoría permanece inmutable y seguro en una red interna diferente.

# Retirada de Itens — Integração com o ESP32 (PROPOSTA)

> **STATUS: PROPOSTA. Nada aqui existe ou foi verificado.** Não analisei o firmware do ESP32 nem nenhum
> backend. O app **não** chama nenhuma destas rotas hoje. Se o firmware já tem protocolo, ele prevalece
> e o app se adapta a ele.

## Perguntas que precisam de resposta antes de implementar
1. O ESP32 é só um leitor/atuador (ex.: destrava um compartimento) ou também guarda o estoque?
2. Como o app fala com ele: HTTP na rede Wi‑Fi local, MQTT (qual broker?), Bluetooth LE ou outro?
3. Onde ficam as retiradas de forma permanente: no ESP32, num servidor/Raspberry, ou só no celular (Room)?
4. Há mais de um operador/dispositivo? Como o operador é identificado (login, PIN, crachá)?
5. Os itens (nome, id, estoque mínimo) vêm de onde? Existe um cadastro?
6. Autenticação na rede local: nenhuma, ou um token configurável no app (nunca fixo no código)?
7. HTTP sem criptografia na LAN é aceitável?

## Se for HTTP (exemplo de contrato — a decidir)
Convenções: JSON UTF‑8; datas ISO 8601 UTC; erros `{"error":{"code":"...","message":"..."}}`.

| Operação | Proposta |
|---|---|
| Status do dispositivo | `GET /api/v1/status` → `{ "name": "...", "firmware": "...", "uptimeSeconds": 0 }` |
| Listar itens e estoque | `GET /api/v1/items` → `{ "items": [ { "id": "...", "name": "...", "stock": 0, "minStock": 0 } ] }` |
| Registrar retirada | `POST /api/v1/withdrawals` com `{ "itemId", "quantity", "priority": "low\|medium\|high", "description", "operator" }` → `{ "id", "timestamp", "remainingStock" }` |
| Histórico | `GET /api/v1/withdrawals?from=&to=&priority=` |
| Registrar reposição | `POST /api/v1/items/{id}/restock` com `{ "quantity" }` |

Imagem anexada: precisa de decisão (enviar multipart, guardar só no celular, ou não enviar), porque o ESP32 tem pouca memória.

## Impacto no app
Criar uma implementação de `InventoryRepository` que use esse contrato e trocar uma linha em
`utils/AppContainer.kt`. As telas e ViewModels não mudam.

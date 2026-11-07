# Sistema de Retirada e Recarga de Itens (Android)

App Android (Kotlin, Jetpack Compose, Material 3, MVVM, StateFlow) para registrar retiradas de itens,
consultar histórico, estoque e relatórios. Pacote: `com.retirada.app`. Visual escuro azul-índigo, com opção de tema claro.

## Como abrir
Android Studio (Ladybug ou mais novo) → *Open* → pasta `RETIRADA` → aguarde o sync → rodar no emulador/aparelho (minSdk 26).

## Estado atual
**Não há comunicação com o ESP32.** Estoque e retiradas existem só em memória no aparelho (itens de exemplo)
e são perdidos ao fechar o app. Tudo é rotulado como demonstração na tela.

| Funcionalidade | Estado |
|---|---|
| Navegação (Início, Histórico, Estoque, Relatórios, Config.), tema escuro/claro/sistema | Implementada e funcional |
| Nova retirada (item, quantidade, prioridade, descrição, imagem opcional, validações) | Implementada e funcional (local, demonstração) |
| Histórico com filtro por prioridade | Implementada e funcional (local, demonstração) |
| Estoque e reposição ("recarga") | Implementada e funcional (local, demonstração) |
| Relatórios (totais, por prioridade, itens mais retirados) | Implementada e funcional (calculados localmente) |
| Nome do operador, endereço/porta do ESP32 (salvos no aparelho) | Implementada e funcional |
| Teste de conexão (apenas TCP host:porta) | Implementada e funcional |
| Comunicação real com o ESP32 / banco de dados persistente | Dependente de definição (protocolo do ESP32 e backend) |

## Arquitetura
`ui (Compose)` → `viewmodel (StateFlow)` → `domain.InventoryRepository` (interface) → `data.DemoInventoryRepository` (hoje).
Para integrar: criar outra implementação de `InventoryRepository` e trocar uma linha em `utils/AppContainer.kt`.

## Testes
`./gradlew test` (regras de retirada/reposição e ViewModels: formulário de retirada, histórico, estoque, relatórios). Código escrito sem Android SDK: **ainda não compilado nem testado**.

## Documentos
`docs/ESP32_CONTRATO_PROPOSTO.md`: perguntas em aberto e proposta de contrato (não verificada).

## Decisões pendentes
- Como o app fala com o ESP32 (HTTP, MQTT, Bluetooth...) e quais comandos/rotas existem.
- Persistência (Room local ou servidor) e autenticação de operadores.
- Tráfego HTTP sem criptografia está liberado (`network_security_config.xml`); restringir ou usar HTTPS.

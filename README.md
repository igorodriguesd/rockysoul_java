# 🌎 RockySoul — Sistema de Gamificação Sustentável

Aplicação de console desenvolvida em **Java 17** com **Maven** que gamifica ações
sustentáveis: o usuário registra ações no histórico, acumula **Pontos ECOA**, sobe de
nível, cumpre missões, conquista selos e acompanha o ranking da semana.

Persistência em **banco Oracle (FIAP)** via JDBC, com o modelo relacional revisado:
`USUARIO`, `AVATAR`, `HISTORICO`, `MISSAO`, `SELO`, `USUARIO_MISSAO` e `USUARIO_SELO`.

📚 **Documentação da entrega (3º Sprint):** veja [`docs/DOCUMENTACAO.md`](docs/DOCUMENTACAO.md)
(capa, sumário, objetivo, funcionalidades, protótipo, MER, diagrama de classes e como rodar).

## 🚀 Como executar

Requisitos: JDK 17+ e Maven (o driver Oracle `ojdbc11` já está no `pom.xml`).

### Opção 1 — Double-click (Windows)

Rode o `run.bat`: ele compila, cria as tabelas no banco e abre o sistema.

### Opção 2 — Terminal

```bash
# 1) criar as tabelas no banco (idempotente — ignora as que já existem)
java -cp target/rockysoul-java-1.0-SNAPSHOT.jar br.com.rockysoulup.database.SchemaSetup

# 2) rodar os testes unitários (não exigem banco)
mvn test

# 3) gerar o fat jar
mvn clean package

# 4) classe de teste: simula a utilização (model + regras + CRUD no Oracle)
java -cp target/rockysoul-java-1.0-SNAPSHOT.jar br.com.rockysoulup.TesteSistema

# 5) rodar o sistema
java -jar target/rockysoul-java-1.0-SNAPSHOT.jar
```

## 🌐 Configuração do banco (rodar em qualquer PC)

Para proteger suas credenciais, **nenhuma senha fica no código nem no repositório**.
Quem clonar o projeto faz apenas:

1. Copie `src/main/resources/db.properties.example` → `src/main/resources/db.properties`;
2. Preencha `DB_USER` e `DB_PASSWORD` (e `DB_URL`, se não for o padrão FIAP);
3. Rode `run.bat` (ou `mvn package` + `java -jar`).

O `db.properties` é ignorado pelo Git (`src/main/resources/db.properties` no
`.gitignore`). O exemplo commitado usa apenas placeholders.

Alternativa sem arquivo: definir as variáveis de ambiente `DB_URL`, `DB_USER` e
`DB_PASSWORD` antes de executar (útil em CI ou em máquinas próprias).

## 🧭 Menu principal

```
1 - Registrar ação sustentável
2 - Ver Meu Nível e Estatísticas
3 - Missões (iniciar / concluir)
4 - Meu Avatar
5 - Ver Ranking da Semana
6 - CRUD              ← Usuários, Missões, Selos e Avatares
0 - Sair
```

Na área do administrador há submenus com Cadastrar / Listar / Atualizar / Excluir.

## 📁 Organização do projeto

| Pacote / pasta | Responsabilidade |
|---|---|
| `br.com.rockysoulup` | `Application` (console) e `TesteSistema` (classe de teste) |
| `model` | Entidades: `Usuario`, `Avatar`, `Historico`, `Missao`, `Selo`, `UsuarioMissao`, `UsuarioSelo` |
| `service` | Regras de negócio (`GamificacaoService`) e orquestração com transações (`RockySoulService`) |
| `dao` + `connection` | Camada JDBC (Oracle) com PreparedStatement e `ConnectionFactory` |
| `database` | `SchemaSetup` (cria as tabelas) e script `schema_revisado_oracle.sql` |
| `src/test` | Testes unitários com JUnit 5 |

## 🏆 Gamificação

**Ações registradas no histórico (pontos):**

| Ação | Pontos |
|---|---|
| Reciclagem | 30 |
| Transporte | 50 |
| Energia | 20 |
| Água | 20 |
| Bicicleta | 40 |
| Plantio de árvore | 100 |
| Banho rápido | 20 |

**Níveis (pela pontuação acumulada):**

SEMENTE → BROTO (100 pts) → ÁRVORE (300) → EXPERT (600)

**Missões** concedem pontos ao serem concluídas (`USUARIO_MISSAO` com status
`PENDENTE`/`EM_ANDAMENTO`/`CONCLUIDA`). **Selos** são concedidos automaticamente
(`USUARIO_SELO`) quando o usuário atinge a pontuação mínima de cada selo.

## ✅ Testes

- **JUnit 5** (`mvn test`): cálculo de nível, progressão, crédito de pontos e selos.
- **`TesteSistema`** (método `main`): instancia as classes modelo, valida as regras
  e exercita o **CRUD completo** de todas as DAOs diretamente no Oracle (cria, lê,
  atualiza, exclui e limpa os dados de teste).

## 🎯 Conceitos aplicados

- DDD/POO: encapsulamento, validações em construtores, getters e setters
- Camadas separadas: interface → serviço (transações) → DAO/JDBC → Oracle
- JDBC com `PreparedStatement`, `getGeneratedKeys` e transações com rollback
- `ConnectionFactory` com usuário e senha no código (padrão FIAP)
- Tratamento de exceções com mensagens amigáveis ao usuário
- Build empacotado com Maven Shade Plugin
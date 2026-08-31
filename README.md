# RockySoul — Sistema de Gamificação Sustentável

Aplicação de **console** em **Java 17 + Maven** que gamifica ações sustentáveis.
O usuário se autentica, registra ações sustentáveis, acumula **Pontos ECOA**, sobe
de nível, conquista **selos** automaticamente, resgata **recompensas reais** na
vitrine (com estoque) e acompanha o **ranking**.

Persistência em **Oracle (FIAP)** via JDBC, com modelo relacional:
`USUARIO`, `ACAO`, `RECOMPENSA`, `HISTORICO`, `SELO` e `USUARIO_SELO`.

## Como abrir no IntelliJ

1. Clone do GitHub (ou abra a pasta baixada).
2. _File_ → _Open_ → selecione a pasta do projeto (requer JDK 17+; o IntelliJ
   importa o `pom.xml` e as dependências automaticamente).
3. Rode a classe `br.com.rockysoulup.Application` (menu principal) ou
   `br.com.rockysoulup.TesteSistema` (suíte de testes).

Não precisa configurar nada para conectar: as credenciais do banco já estão no código
(veja "Banco de dados" abaixo).

## Banco de dados

- URL: `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL`
- As credenciais (**usuário e senha**) estão **inseridas no código**, na classe
  `ConnectionFactory` (conta FIAP padrão `rm570651`) — exigência da rubrica da entrega.
- Para usar outro acesso, crie uma cópia de `src/main/resources/db.properties.example`
  chamada `db.properties` e preencha `DB_USER`/`DB_PASSWORD`. Esse arquivo é ignorado
  pelo Git (não vai pro repositório) e o exemplo commitado tem apenas placeholders.
- Alternativa via variáveis de ambiente: `DB_URL`, `DB_USER` e `DB_PASSWORD`.

## Como executar

Requisitos: JDK 17+ e Maven (o driver Oracle `ojdbc11` já está no `pom.xml`).

```bash
# 1) gerar o fat jar (roda os testes unitários JUnit)
mvn clean package

# 2) suíte de testes de integração no Oracle (cria, valida e limpa os dados)
java -cp target/rockysoul-java-1.0-SNAPSHOT.jar br.com.rockysoulup.TesteSistema
java -cp target/rockysoul-java-1.0-SNAPSHOT.jar br.com.rockysoulup.TesteCrudCompleto

# 3) rodar o sistema
java -jar target/rockysoul-java-1.0-SNAPSHOT.jar
```

No Windows também existe o `run.bat` (compila e abre o sistema).

> Importante: `TesteSistema` e `TesteCrudCompleto` usam dados de teste no mesmo banco.
> Rode-os **em sequência, nunca em paralelo**, e sempre execute o projeto sozinho.

## Menu

```
===== MENU PRINCIPAL =====
1 - Uso do sistema
2 - Área de cadastro (CRUD)
0 - Sair
```

`1 - Uso do sistema` pede nome/e-mail (cria ou localiza o usuário) e abre o dashboard:

```
===== DASHBOARD SOULUP =====   [ Pontos: X | Resgatados: Y | Nível: Z ]
1 - Registrar ação sustentável
2 - Ver meu nível e estatísticas
3 - Resgatar recompensas (benefícios reais)
4 - Sugestão do avatar
5 - Ver ranking da semana
0 - Voltar
```

`2 - Área de cadastro (CRUD)` gerencia **usuários**, **ações** e **recompensas**.

## Catálogo padrão (semeado na primeira execução)

**Ações sustentáveis:**

| Ação | Pontos |
|---|---|
| Economia de Água | 15 |
| Economia de Energia | 20 |
| Banho Rápido | 20 |
| Bicicleta | 25 |
| Reciclagem | 30 |
| Transporte Público | 50 |
| Plantio de Árvore | 100 |

**Recompensas (custo em pontos — estoque limitado):**

| Recompensa | Custo | Estoque | Categoria | Destaque |
|---|---|---|---|---|
| Cupom Reciclagem | 150 | 30 | Cupons | |
| Desconto Água | 180 | 30 | Energia | |
| Desconto Energia | 200 | 50 | Energia | Novo |
| Kit Sustentável | 250 | 20 | Natureza | |
| Passe de Transporte | 350 | 15 | Transporte | |
| Cupom Bicicleta | 400 | 10 | Transporte | |
| Muda de Árvore | 500 | 20 | Natureza | Top 1 |
| Adoção de Árvore | 800 | 2 | Natureza | Top 1 |

**Selos (conquistados automaticamente ao atingir os pontos mínimos):**

| Selo | Pontos mínimos |
|---|---|
| Semente | 100 |
| Broto | 300 |
| Árvore | 600 |
| Expert | 1000 |

**Níveis (pela pontuação acumulada):**

`SEMENTE → BROTO (100) → ÁRVORE (300) → EXPERT (600)`

## Organização do projeto

| Pacote / pasta | Responsabilidade |
|---|---|
| `br.com.rockysoulup` | `Application` (menu/console) e as suítes `TesteSistema`, `TesteCrudCompleto` |
| `model` | Entidades: `Usuario`, `Historico`, `Selo`, `UsuarioSelo`, `Acao`, `Recompensa` |
| `service` | Regras de negócio: `GamificacaoService` e `RockySoulService` (orquestração com transações) |
| `dao` | Camada JDBC: CRUD completo para as 6 entidades |
| `connection` | `ConnectionFactory` (conexão Oracle com credenciais no código) |
| `database` | `SchemaSetup` (cria as tabelas) e script `database/schema_revisado_oracle.sql` |
| `src/test` | Testes unitários JUnit 5 (sem banco) |

## Testes

- **JUnit 5** (`mvn test`): cálculo de nível, progressão e crédito de pontos/selos.
- **`TesteSistema`** (método `main`): model, regras de negócio, CRUD e o cenário de
  resgate de recompensa (saldo insuficiente → junta pontos → resgata → estoque zera) no Oracle.
- **`TesteCrudCompleto`** (método `main`): 10 usuários simulando o uso real, ranking,
  atualizações e exclusões em cascata, com limpeza total ao final.

## Conceitos aplicados

- DDD/POO: encapsulamento, validações em construtores, getters e setters.
- Camadas separadas: interface (console) → serviço (regras + transações) → DAO/JDBC → Oracle.
- JDBC com `PreparedStatement`, `getGeneratedKeys` e transações com rollback.
- Regras de negócio reais: e-mail único, conquista automática de selos, resgate atômico
  (baixa de estoque + desconto de pontos + resgatados) e exclusões em cascata.
- `ConnectionFactory` com usuário e senha inseridos no código (padrão FIAP/rubrica).
- Build empacotado com Maven Shade Plugin (`run.bat`).

## Integrantes

| Nome | RM |
|---|---|
| Igor Rodrigues de Santana | RM570651 |
| Diego Gomes Goncalves de Lima | RM570335 |
| Miguel Silva | RM572019 |
| Rafael Santos Mendonca Costa | RM572368 |
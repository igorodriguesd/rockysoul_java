# RockySoul — Sistema de Gamificação Sustentável

Aplicação **Java 21 + Maven** que combina gestão de usuários, ações sustentáveis,
recompensas e um sistema de cartas colecionáveis inspirados no modelo de jogo do
frontend React. O usuário acumula pontos, evolui por níveis, conquista selos,
coleta cartas com raridades distintas, fabrica cartas com fragmentos, resgata
benefícios reais e acompanha o ranking da comunidade.

Persistência em **Oracle via JDBC**, com 8 tabelas relacionadas entre si.

## Funcionalidades principais

- Cadastro e autenticação de usuários (regra de e-mail único)
- Catálogo de ações sustentáveis com pontuação (1–100 pts)
- Registro de ação: soma pontos, grava histórico e conquista selos
- Progressão por nível (SEMENTE, BROTO, ÁRVORE, EXPERT)
- Conquista automática de selos conforme a pontuação
- Resgate de recompensas com estoque, validação de saldo e transação
- Sistema de cartas colecionáveis por raridade
- Coleção por usuário com quantidade por carta
- Sorteio de cartas ao registrar ações (drop por raridade)
- Fragmentos gerados por cartas repetidas (fabricação de cartas novas)
- Ranking de usuários (mais pontos primeiro)
- Área administrativa de CRUD (usuários, ações, recompensas e cartas)

## Níveis do usuário

| Nível | Pontuação acumulada mínima |
|---|---:|
| SEMENTE | 0 |
| BROTO | 100 |
| ÁRVORE | 300 |
| EXPERT | 600 |

## Sistema de cartas

A raridade é a regra principal do gameplay: sorteio com probabilidades fixas,
fragmentos por carta repetida e custo de fabricação por raridade — tudo centralizado
no `RockySoulService`.

### Raridades suportadas

| Raridade | Chance de aparecimento | Fragmentos por carta | Custo p/ fabricar |
|---|---:|---:|---:|
| COMUM | 75% | 3 | 5 |
| INCOMUM | 15% | 5 | 10 |
| RARA | 6% | 10 | 20 |
| EPICA | 3% | 20 | 30 |
| LENDARIA | 1% | 40 | 40 |

### Catálogo padrão de cartas

O catálogo é semeado automaticamente na primeira execução, quando o banco está
vazio (`garantirCatalogo()`). Inclui 20 cartas em 5 conjuntos temáticos:

- **Recursos**: Reciclagem, Reutilização, Sacola Reutilizável, Redução de Desperdício
- **Água**: Economia de Água, Banho Rápido, Garrafa Reutilizável, Captação de Chuva
- **Mobilidade**: Bicicleta, Transporte Público, Mobilidade Elétrica, Ciclovia
- **Energia**: Economia de Energia, Iluminação Eficiente, Energia Solar, Energia Eólica
- **Cultivo**: Plantio, Compostagem, Horta Doméstica, Agrofloresta

## Requisitos

| Ferramenta | Versão |
|---|---|
| JDK (Java) | 21 ou superior |
| Maven | 3.9 ou superior |
| Banco | Oracle (FIAP) — acessível pela rede acadêmica/UFIAP |
| IDE | IntelliJ IDEA, Eclipse, NetBeans ou VS Code |

## Como abrir no IntelliJ

1. Clone o repositório ou abra a pasta local (`File → Open`).
2. O IntelliJ importa o Maven automaticamente (aguarde o download das deps).
3. Rode a classe `br.com.rockysoulup.Application` para acessar o menu principal.
4. Antes, garanta o schema do banco (seção abaixo).

## Banco de dados

- URL: `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL`
- As credenciais do banco estão configuradas na classe `ConnectionFactory`
  (entrega FIAP exige conexão sem input do usuário).
- É possível sobrepor via variáveis de ambiente `DB_URL`, `DB_USER` e `DB_PASSWORD`
  ou via arquivo `db.properties` (veja `src/main/resources/db.properties.example`).
- O schema possui 8 tabelas:

  | Tabela | Descrição | Chaves/restrições |
  |---|---|---|
  | USUARIO | Usuários do sistema | PK `ID_USUARIO`, UNIQUE `DS_EMAIL`, pontos >= 0 |
  | ACAO | Catálogo de ações sustentáveis | PK `ID_ACAO`, pontos 1–100 |
  | RECOMPENSA | Benefícios resgatáveis | PK `ID_RECOMPENSA`, custo > 0, estoque >= 0 |
  | HISTORICO | Ações registradas por usuário | PK `ID_HISTORICO`, FK `ID_USUARIO` → USUARIO |
  | SELO | Selos por faixa de pontos | PK `ID_SELO`, `NR_PONTOS_MIN >= 0` |
  | CARTA | Catálogo de cartas | PK `ID_CARTA`, raridade em (COMUM, INCOMUM, RARA, EPICA, LENDARIA) |
  | USUARIO_CARTA | Cartas de cada usuário | PK composta `(ID_USUARIO, ID_CARTA)`, FKs p/ USUARIO e CARTA |
  | USUARIO_FRAGMENTO | Fragmentos por raridade | PK composta `(ID_USUARIO, DS_RARIDADE)`, FK p/ USUARIO |

### Criar/atualizar o schema (opcional)

O `SchemaSetup` cria as tabelas idempotentemente (ignora ORA-00955):

```bash
mvn clean package
java -cp target/rockysoul-java-1.0-SNAPSHOT.jar br.com.rockysoulup.database.SchemaSetup
```

## Como executar

### Terminal

```bash
# 1) compilar e rodar os testes de unidade
mvn clean test

# 2) rodar os testes de integração no Oracle
mvn test -Pintegration

# 3) gerar o artefato
mvn clean package

# 4) executar o sistema
java -jar target/rockysoul-java-1.0-SNAPSHOT.jar
```

No Windows também existe o `run.bat` para facilitar a execução.

### IDE

1. Abra o projeto (Maven import).
2. Aguarde o `Resolve project`/`Reload`.
3. Execute `br.com.rockysoulup.Application`.

## Menu principal

```text
===== MENU PRINCIPAL =====
1 - Uso do sistema
2 - Área de cadastro (CRUD)
0 - Sair
```

### Uso do sistema (usuário)

```text
Base de dados: oracle.fiap.com.br:1521/ORCL   (schema rm570651)

===== DASHBOARD SOULUP =====   [ Pontos: X | Resgatados: Y | Nível: Z ]
1 - Registrar ação sustentável
2 - Ver meu nível e estatísticas
3 - Resgatar recompensas (benefícios reais)
4 - Minha coleção de cartas
0 - Voltar
```

### Área de cadastro (CRUD / admin)

```text
===== ÁREA DE CADASTRO (CRUD) =====
1 - Gerenciar usuários
2 - Gerenciar ações
3 - Gerenciar recompensas
4 - Gerenciar cartas
0 - Voltar
```

Cada gestão oferece incluir, alterar, excluir e listar. Ações e recompensas
também têm filtro/consulta por pontos e por nome/título.

## Catálogo base do sistema

### Ações sustentáveis

| Ação | Pontos |
|---|---:|
| Economia de Água | 15 |
| Economia de Energia | 20 |
| Banho Rápido | 20 |
| Bicicleta | 25 |
| Reciclagem | 30 |
| Transporte Público | 50 |
| Plantio de Árvore | 100 |

### Recompensas

| Recompensa | Custo | Estoque | Categoria | Destaque |
|---|---:|---:|---|---|
| Cupom Reciclagem | 150 | 30 | Cupons | |
| Desconto Água | 180 | 30 | Energia | |
| Desconto Energia | 200 | 50 | Energia | Novo |
| Kit Sustentável | 250 | 20 | Natureza | |
| Passe de Transporte | 350 | 15 | Transporte | |
| Cupom Bicicleta | 400 | 10 | Transporte | |
| Muda de Árvore | 500 | 20 | Natureza | Top 1 |
| Adoção de Árvore | 800 | 2 | Natureza | Top 1 |

### Selos

| Selo | Pontos mínimos |
|---|---:|
| Semente | 100 |
| Broto | 300 |
| Árvore | 600 |
| Expert | 1000 |

## Estrutura do projeto

| Pacote / pasta | Responsabilidade |
|---|---|
| `br.com.rockysoulup` | `Application` (menu) |
| `model` | Entidades: `Usuario`, `Historico`, `Selo`, `Acao`, `Recompensa`, `Carta`, `UsuarioCarta`, `UsuarioFragmento` |
| `service` | `GamificacaoService` (regras puras) e `RockySoulService` (orquestração JDBC) |
| `repository` | CRUD via JDBC: um repositório por entidade |
| `connection` | `ConnectionFactory` |
| `database` | `SchemaSetup` (DDL idempotente) |
| `exception` | `RegistroDuplicadoException`, `SaldoInsuficienteException` |
| `src/test` | Testes de unidade e de integração |

## Exceções de negócio

- `RegistroDuplicadoException` — lançada quando já existe usuário com o mesmo e-mail.
- `SaldoInsuficienteException` — lançada no resgate quando os pontos são insuficientes
  e na fabricação quando não há fragmentos.

## Testes

- **Unidade** (`mvn test`): lógica de raridade, probabilidades de drop, fragmentos,
  catálogo e condições de validação — 10 testes JUnit 5, não dependem do banco.
- **Integração** (`mvn test -Pintegration`): `TesteSistema` (fluxo completo no Oracle)
  e `TesteCrudCompleto` (CRUD de todas as entidades).

## Conceitos aplicados

- Orientação a objetos e encapsulamento (setters validam o domínio)
- Separação por camadas: interface (menu), serviço, repositório e banco
- JDBC com `PreparedStatement`, transações e exclusões em cascata no serviço
- Regras de negócio reais: e-mail único, selo automático, drop de cartas, estoque
- Modelo de cartas alinhado ao React, com raridade como eixo principal do drop
- Tratamento de erros `SQLException` convertidos em exceções de negócio

## Integrantes

| Nome | RM |
|---|---|
| Igor Rodrigues de Santana | RM570651 |
| Diego Gomes Goncalves de Lima | RM570335 |
| Miguel Silva | RM572019 |
| Rafael Santos Mendonca Costa | RM572368 |
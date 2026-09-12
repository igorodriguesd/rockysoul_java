# RockySoul — Sistema de Gamificação Sustentável

Aplicação Java 21 + Maven que combina gestão de usuários, ações sustentáveis,
recompensas e um sistema de cartas colecionáveis inspirados no modelo de jogo do
frontend React. O usuário acumula pontos, evolui por selo, coleta cartas com
raridades distintas, resgata benefícios reais e acompanha o ranking da comunidade.

Persistência em Oracle via JDBC, com relacionamento entre usuários, ações,
recompensas, histórico, selos, cartas e posse de cartas.

## Funcionalidades principais

- Registro e autenticação de usuários
- Cadastro de ações sustentáveis com pontuação
- Progressão por nível e conquista automática de selos
- Resgate de recompensas com estoque e validação de saldo
- Sistema de cartas colecionáveis por raridade
- Coleção por usuário com quantidade por carta
- Catalogação de cartas e sorteio por raridade
- Ranking e histórico de atividades

## Sistema de cartas

As cartas foram modeladas para refletir a lógica do projeto React, sem a criação
separada de um atributo "brilhante". A raridade é a regra principal, e a visual
variação do card não precisa ser tratada como categoria de gameplay.

### Raridades suportadas

| Raridade | Chance de aparecimento | Fragmentos por carta |
|---|---:|---:|
| COMUM | 75% | 3 |
| INCOMUM | 15% | 5 |
| RARA | 6% | 10 |
| EPICA | 3% | 20 |
| LENDARIA | 1% | 40 |

A lógica de sorteio usa a raridade como critério principal e escolhe uma carta do
catálogo dentro da raridade sorteada. Cartas raras são mais difíceis de obter e
geram mais fragmentos ao serem repetidas, em linha com o comportamento do jogo.

### Catálogo padrão de cartas

O catálogo é semeado automaticamente na primeira execução, quando o banco está
vazio. Ele inclui cartas de diferentes conjuntos temáticos, como recursos,
conservação de água, mobilidade, energia limpa e cultivo.

Exemplos:

- Recurso: Reciclagem, Reutilização, Sacola Reutilizável, Redução de Desperdício
- Água: Economia de Água, Banho Rápido, Garrafa Reutilizável, Captação de Chuva
- Mobilidade: Bicicleta, Transporte Público, Mobilidade Elétrica, Ciclovia
- Energia: Economia de Energia, Iluminação Eficiente, Energia Solar, Energia Eólica
- Cultivo: Plantio, Compostagem, Horta Doméstica, Agrofloresta

## Como abrir no IntelliJ

1. Clone o repositório ou abra a pasta local.
2. Vá em File → Open e selecione a pasta do projeto.
3. O IntelliJ importará o Maven automaticamente.
4. Rode a classe `br.com.rockysoulup.Application` para acessar o menu principal.

## Banco de dados

- URL: `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL`
- As credenciais do banco estão configuradas na classe `ConnectionFactory`, conforme a estrutura da entrega FIAP.
- Também é possível usar um arquivo `db.properties` com base no exemplo em `src/main/resources/db.properties.example`.
- Alternativa: variáveis de ambiente `DB_URL`, `DB_USER` e `DB_PASSWORD`.

## Como executar

Requisitos: JDK 21+ e Maven.

```bash
# 1) compilar e rodar os testes
mvn clean test

# 2) gerar o artefato
mvn clean package

# 3) executar o sistema
java -jar target/rockysoul-java-1.0-SNAPSHOT.jar
```

No Windows também existe o arquivo `run.bat` para facilitar a execução.

## Menu principal

```text
===== MENU PRINCIPAL =====
1 - Uso do sistema
2 - Área de cadastro (CRUD)
0 - Sair
```

### Dashboard do usuário

```text
===== DASHBOARD SOULUP =====   [ Pontos: X | Resgatados: Y | Nível: Z ]
1 - Registrar ação sustentável
2 - Ver meu nível e estatísticas
3 - Resgatar recompensas
4 - Sugestão do avatar
5 - Ver ranking da semana
0 - Voltar
```

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
| `br.com.rockysoulup` | `Application`, `TesteSistema` e `TesteCrudCompleto` |
| `model` | Entidades do domínio: `Usuario`, `Historico`, `Selo`, `UsuarioSelo`, `Acao`, `Recompensa`, `Carta`, `UsuarioCarta` |
| `service` | `GamificacaoService` e `RockySoulService` |
| `repository` | CRUD via JDBC |
| `connection` | `ConnectionFactory` |
| `database` | `SchemaSetup` e scripts do banco |
| `src/test` | Testes de unidade e integração |

## Testes

- JUnit 5, via `mvn test`
- Validação de nível, pontuação e conquista de selos
- Testes da lógica de raridade e catálogo de cartas
- Execução de cenários no Oracle para validação de integração

## Conceitos aplicados

- Orientação a objetos e encapsulamento
- Separação por camadas: interface, serviço, repositório e banco
- Uso de JDBC com `PreparedStatement` e transações
- Regras de negócio reais: e-mail único, selo automático, atualização de pontos e exclusões em cascata
- Modelo de cartas alinhado ao React, com raridade como eixo principal do drop

## Integrantes

| Nome | RM |
|---|---|
| Igor Rodrigues de Santana | RM570651 |
| Diego Gomes Goncalves de Lima | RM570335 |
| Miguel Silva | RM572019 |
| Rafael Santos Mendonca Costa | RM572368 |

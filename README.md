# 🌎 RockySoul — Sistema de Gamificação Sustentável

Aplicação de console desenvolvida em **Java 17** com **Maven** que gamifica ações
sustentáveis: o usuário registra missões ecológicas, acumula **Pontos ECOA**, sobe de
nível, acompanha o ranking da semana e troca pontos por **benefícios reais**.

## 🚀 Como executar

Requisitos: JDK 17+ e Maven.

```bash
# rodar os testes
mvn test

# gerar o jar executável (fat jar)
mvn clean package

# rodar o sistema
java -jar target/rockysoul-java-1.0-SNAPSHOT.jar
```

## 🧭 Menu principal

```
1 - Registrar ação sustentável
2 - Ver Meu Nível e Estatísticas
3 - Resgatar Recompensas (Benefícios Reais)
4 - Sugestão do Avatar
5 - Ver Ranking da Semana
6 - Área do administrador   ← CRUD completo (protegido por senha)
0 - Sair
```

Na área do administrador há submenus de **Usuários**, **Ações sustentáveis** e
**Recompensas**, cada um com Cadastrar / Listar / Atualizar / Excluir.

## 📁 Organização do projeto

| Pacote / pasta | Responsabilidade |
|---|---|
| `br.com.rockysoulup` (`Application`) | Interface de console, menus e validação de entrada |
| `model` | Entidades: `Usuario`, `AcaoSustentavel`, `Recompensa`, `RegistroAcao` |
| `service` | Regras de negócio (`GamificacaoService`: registrar ação, resgatar) |
| `repository` | Persistência em JSON com Gson (`JsonDatabase`) |
| `dao` + `connection` | Camada JDBC legada (Oracle), mantida para migração futura |
| `database` | Scripts SQL (Oracle/SQLite) e backup de dados |
| `src/test` | Testes unitários com JUnit 5 |

## 💾 Persistência

Os dados são gravados automaticamente em **JSON** (biblioteca **Gson**) no caminho fixo:

```
%USERPROFILE%\.rockysoulup\data.json
```

Ao iniciar, a base é normalizada (remove duplicidades, recalcula contadores) e semeia,
de forma idempotente, as ações e recompensas padrão caso não existam.

## 🏆 Gamificação

**Ações sustentáveis (pontos):**

| Ação | Pontos |
|---|---|
| Reciclagem | 30 |
| Transporte | 50 |
| Energia | 20 |
| Água | 20 |
| Bicicleta | 40 |
| Plantio de árvore | 100 |
| Banho rápido | 20 |

**Níveis (7 tiers):**

RAIZ DE NADA → BROTO (100 pts) → GRAVETO (200) → ERVA (300)
→ MATO ALTO (400) → TRONCO (500) → JARDINEIRO DO EDEN (600)

**Vitrine de recompensas:** de *Plantar 1 Árvore* (200 pts) até
*Mês de Energia Grátis* (2000 pts), com controle de estoque e saldo.

## ✅ Testes

Testes unitários (JUnit 5) cobrem cálculo de nível/ranking nas 7 faixas,
progressão para o próximo nível, acúmulo de pontos resgatados e fluxo de resgate:

```bash
mvn test
```

## 🎯 Conceitos aplicados

- Programação orientada a objetos: encapsulamento, validações em construtores/métodos
- Camadas separadas: interface → serviço → repositório/persistência
- Coleções e Streams API para buscas e filtros
- Tratamento de exceções com mensagens amigáveis ao usuário
- Persistência em arquivo JSON com normalização idempotente
- Testes automatizados com JUnit 5 e build empacotado com Maven Shade Plugin

package br.com.rockysoulup;

import br.com.rockysoulup.model.*;
import br.com.rockysoulup.service.GamificacaoService;

public class TesteSistema {

  public static void main(String[] args) {
    GamificacaoService service = new GamificacaoService();

    System.out.println("========== TESTE DO SISTEMA SOULUP ==========\n");

    // 1. Cadastro de usuários
    System.out.println("--- 1. Cadastrando usuários ---");
    Usuario ana = new Usuario("Ana", "ana@email.com");
    Usuario bruno = new Usuario("Bruno", "bruno@email.com");
    System.out.println("Usuário criado: " + ana);
    System.out.println("Usuário criado: " + bruno);

    // 2. Níveis iniciais
    System.out.println("\n--- 2. Verificando nível inicial ---");
    System.out.println("Ana nível: " + ana.getNivel() + " (esperado: SEMENTE)");
    System.out.println("Próximo nível: " + ana.proximoNivel() + " (esperado: BROTO)");
    System.out.println("Faltam: " + ana.pontosParaProximoNivel() + " pts (esperado: 100)");

    // 3. Registro de ações sustentáveis
    System.out.println("\n--- 3. Registrando ações sustentáveis ---");
    AcaoSustentavel reciclagem = new AcaoSustentavel("Reciclagem", "Separar resíduos", 30);
    AcaoSustentavel plantio = new AcaoSustentavel("Plantio de árvore", "Plantar uma árvore", 100);
    AcaoSustentavel bicicleta = new AcaoSustentavel("Bicicleta", "Pedalar em vez de carro", 40);

    service.registrarAcao(ana, reciclagem);
    System.out.println("Ana registrou: Reciclagem (+30 pts)");
    System.out.println("Saldo Ana: " + ana.getPontos() + " pts");

    service.registrarAcao(ana, plantio);
    System.out.println("Ana registrou: Plantio de árvore (+100 pts)");
    System.out.println("Saldo Ana: " + ana.getPontos() + " pts");
    System.out.println("Nível Ana: " + ana.getNivel() + " (esperado: BROTO)");

    service.registrarAcao(bruno, bicicleta);
    System.out.println("Bruno registrou: Bicicleta (+40 pts)");
    System.out.println("Saldo Bruno: " + bruno.getPontos() + " pts");
    System.out.println("Nível Bruno: " + bruno.getNivel() + " (esperado: SEMENTE)");

    // 4. Evolução de nível
    System.out.println("\n--- 4. Testando evolução de nível ---");
    service.registrarAcao(ana, plantio);
    service.registrarAcao(ana, plantio);
    service.registrarAcao(ana, plantio);
    service.registrarAcao(ana, plantio);
    service.registrarAcao(ana, plantio);
    System.out.println("Ana registrou 5x Plantio (+500 pts)");
    System.out.println("Saldo Ana: " + ana.getPontos() + " pts (esperado: 630)");
    System.out.println("Nível Ana: " + ana.getNivel() + " (esperado: EXPERT)");
    System.out.println("Próximo nível: " + ana.proximoNivel() + " (esperado: null)");
    System.out.println("Faltam: " + ana.pontosParaProximoNivel() + " pts (esperado: -1)");

    // 5. Resgate de recompensas
    System.out.println("\n--- 5. Testando resgate de recompensas ---");
    Recompensa kitReciclagem = new Recompensa("Kit Reciclagem", "Kit doméstico", 250, 20);
    Recompensa arvore = new Recompensa("Plantar 1 árvore", "Muda nativa", 200, 50);
    System.out.println("Saldo Ana: " + ana.getPontos() + " pts");

    service.resgatar(ana, kitReciclagem);
    System.out.println("Ana resgatou: Kit Reciclagem (-250 pts)");
    System.out.println("Saldo Ana: " + ana.getPontos() + " pts (esperado: 380)");
    System.out.println("Resgatados Ana: " + ana.getPontosResgatados() + " pts (esperado: 250)");
    System.out.println("Estoque Kit: " + kitReciclagem.getEstoque() + " (esperado: 19)");

    service.resgatar(ana, arvore);
    System.out.println("Ana resgatou: Plantar 1 árvore (-200 pts)");
    System.out.println("Saldo Ana: " + ana.getPontos() + " pts (esperado: 180)");
    System.out.println("Resgatados Ana: " + ana.getPontosResgatados() + " pts (esperado: 450)");

    // 6. Erro: saldo insuficiente
    System.out.println("\n--- 6. Testando erro de saldo insuficiente ---");
    Recompensa energiaGratis = new Recompensa("Mês de Energia Grátis", "Conta zerada", 2000, 1);
    try {
      service.resgatar(ana, energiaGratis);
      System.out.println("ERRO: deveria ter lançado exceção!");
    } catch (IllegalStateException e) {
      System.out.println("Exceção capturada corretamente: " + e.getMessage());
    }

    // 7. Erro: estoque zero
    System.out.println("\n--- 7. Testando erro de estoque zero ---");
    Recompensa estoqueZero = new Recompensa("Teste Esgotado", "Descrição", 50, 0);
    try {
      service.resgatar(bruno, estoqueZero);
      System.out.println("ERRO: deveria ter lançado exceção!");
    } catch (IllegalStateException e) {
      System.out.println("Exceção capturada corretamente: " + e.getMessage());
    }

    // 8. Validações do model
    System.out.println("\n--- 8. Testando validações ---");
    try {
      new Usuario("", "email@email.com");
    } catch (IllegalArgumentException e) {
      System.out.println("Nome vazio: " + e.getMessage());
    }
    try {
      new Usuario("Teste", "email-invalido");
    } catch (IllegalArgumentException e) {
      System.out.println("Email inválido: " + e.getMessage());
    }
    try {
      new AcaoSustentavel("Teste", "Desc", -10);
    } catch (IllegalArgumentException e) {
      System.out.println("Pontos negativos: " + e.getMessage());
    }
    try {
      new Recompensa("Teste", "Desc", 100, -1);
    } catch (IllegalArgumentException e) {
      System.out.println("Estoque negativo: " + e.getMessage());
    }

    // 9. Resumo final
    System.out.println("\n========== RESUMO ==========");
    System.out.println("Ana: " + ana.getPontos() + " pts | " + ana.getPontosResgatados() + " resgatados | " + ana.getNivel());
    System.out.println("Bruno: " + bruno.getPontos() + " pts | " + bruno.getPontosResgatados() + " resgatados | " + bruno.getNivel());
    System.out.println("\nTodos os testes executados com sucesso!");
  }
}

package br.com.rockysoulup;

import br.com.rockysoulup.dao.*;
import br.com.rockysoulup.model.*;
import br.com.rockysoulup.service.GamificacaoService;
import br.com.rockysoulup.service.RockySoulService;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe de teste: método main que instancia as classes modelo, valida as
 * regras de negócio e exercita o CRUD completo (Create/Read/Update/Delete)
 * da camada DAO diretamente no banco Oracle, simulando a utilização da
 * aplicação.
 */
public class TesteSistema {

  private static final List<Runnable> LIMPEZA = new ArrayList<>();

  public static void main(String[] args) throws Exception {
    System.out.println("========== TESTE DO SISTEMA SOULUP (DDD / JDBC / Oracle) ==========\n");

    testeModelo();
    testeRegrasDeNegocio();

    try {
      testeCrudNoBanco();
    } finally {
      limpar();
    }

    System.out.println("\nTodos os cenários executados com sucesso!");
  }

  /** Cenário 1: camada modelo — atributos, construtores, validações. */
  private static void testeModelo() {
    System.out.println("--- 1. Camada Model: instanciação e validações ---");
    Usuario u = new Usuario("Ana", "ana@email.com");
    Selo s = new Selo("Broto", "50 pontos", 50);
    Historico h = new Historico(1L, "Reciclagem", 30);
    UsuarioSelo us = new UsuarioSelo(1L, 1L);
    System.out.println("Objetos criados: " + u + " | " + s.getNome()
      + " | " + h.getDescricao() + " | seloId=" + us.getSeloId());

    for (Runnable invalido : List.<Runnable>of(
      () -> new Usuario("", "x@x.com"),
      () -> new Usuario("Ana", "email-invalido"),
      () -> new Historico(1L, "Ação", 200),
      () -> new Selo("S", "D", -1)
    )) {
      try {
        invalido.run();
        System.out.println("ERRO: deveria rejeitar a validação!");
      } catch (IllegalArgumentException e) {
        System.out.println("Validação ok -> " + e.getMessage());
      }
    }
  }

  /** Cenário 2: lógica de negócio (GamificacaoService, sem banco). */
  private static void testeRegrasDeNegocio() {
    System.out.println("\n--- 2. Métodos com lógica de negócio ---");
    GamificacaoService service = new GamificacaoService();

    Usuario ana = new Usuario("Ana", "ana@email.com");
    service.registrarAcao(ana, 30);
    if (ana.getPontos() != 30 || !"SEMENTE".equals(ana.getNivel())) {
      throw new IllegalStateException("Falha: pontos ou nível após ação");
    }
    System.out.println("registrarAcao: +30 pts, nível SEMENTE -> ok");

    ana.adicionarPontos(70);
    if (!"BROTO".equals(ana.getNivel())) throw new IllegalStateException("Falha: nível BROTO");
    System.out.println("Evolução de nível em 100 pts -> BROTO -> ok");

    Selo broto = new Selo("Broto", "50 pts", 50);
    Selo exper = new Selo("Expert", "600 pts", 600);
    if (!service.seloConquistado(ana, broto) || service.seloConquistado(ana, exper)) {
      throw new IllegalStateException("Falha: regra de selo");
    }
    System.out.println("seloConquistado: Broto=true, Expert=false -> ok");

    System.out.println("Próximo nível: " + ana.proximoNivel()
      + " (faltam " + ana.pontosParaProximoNivel() + " pts)");
  }

  /** Cenário 3: DAO + Service contra o banco Oracle — CRUD completo. */
  private static void testeCrudNoBanco() throws Exception {
    String suf = String.valueOf(System.currentTimeMillis());
    UsuarioDao usuarioDao = new UsuarioDao();
    HistoricoDao historicoDao = new HistoricoDao();
    SeloDao seloDao = new SeloDao();
    UsuarioSeloDao usDao = new UsuarioSeloDao();
    RockySoulService service = new RockySoulService();

    System.out.println("\n--- 3. CRUD Usuario (Create/Read/Update/Delete) ---");
    Usuario crudUser = new Usuario("Crud Teste " + suf, "crud." + suf + "@email.com");
    usuarioDao.inserir(crudUser);
    System.out.println("Create -> id " + crudUser.getId());
    Usuario lido = usuarioDao.buscarPorId(crudUser.getId());
    System.out.println("Read (por id) -> " + lido);
    System.out.println("Read (por e-mail) -> " + usuarioDao.buscarPorEmail(crudUser.getEmail()).getNome());
    lido.setNome("Crud Renomeado " + suf);
    usuarioDao.atualizar(lido);
    System.out.println("Update -> " + usuarioDao.buscarPorId(crudUser.getId()).getNome());
    usuarioDao.excluir(crudUser.getId());
    System.out.println("Delete -> " + (usuarioDao.buscarPorId(crudUser.getId()) == null ? "removido" : "ERRO"));

    System.out.println("\n--- 4. CRUD Selo ---");
    Selo selo = new Selo("Selo teste " + suf, "descrição", 10);
    seloDao.inserir(selo);
    selo.setNome("Selo renomeado " + suf);
    seloDao.atualizar(selo);
    System.out.println("Create/Read/Update -> " + seloDao.buscarPorId(selo.getId()).getNome());
    seloDao.excluir(selo.getId());
    System.out.println("Delete -> ok");

    System.out.println("\n--- 5. Fluxo da aplicação (login, ação, selo) ---");
    String email = "ana.teste." + suf + "@email.com";
    Usuario ana = service.cadastrarUsuario("Ana Teste " + suf, email);
    System.out.println("login/cadastro -> id " + ana.getId() + ", pontos " + ana.getPontos());

    try {
      service.cadastrarUsuario("Outra Ana " + suf, email);
      throw new IllegalStateException("Falha: e-mail duplicado deveria ser rejeitado");
    } catch (IllegalStateException esperado) {
      System.out.println("e-mail duplicado rejeitado -> ok");
    }

    Selo seloInicio = new Selo("Selo início " + suf, "mínimo 0", 0);
    seloDao.inserir(seloInicio);

    List<Selo> concedidos = service.registrarAcao(ana, "Reciclagem", 30);
    System.out.println("registrarAcao -> pontos " + ana.getPontos()
      + ", selos concedidos: " + nomes(concedidos));

    Historico historico = historicoDao.listarPorUsuario(ana.getId()).get(0);
    historico.setDescricao("Ação editada");
    historicoDao.atualizar(historico);
    System.out.println("historico update -> " + historicoDao.listarPorUsuario(ana.getId()).get(0).getDescricao());

    LIMPEZA.add(() -> limparFluxo(usuarioDao, historicoDao, usDao,
      seloDao, ana, seloInicio.getId()));

    System.out.println("\n--- 6. Ranking e selos ---");
    System.out.println("selos do usuário -> " + nomes(service.listarSelosConcedidos(ana)));
    List<Usuario> ranking = usuarioDao.listar();
    System.out.println("ranking (top 5): " + ranking.stream().limit(5)
      .map(u -> u.getNome() + ":" + u.getPontos()).reduce((x, y) -> x + " | " + y).orElse("-"));

    System.out.println("\n--- 7. Resgate de recompensa (vitrine com estoque) ---");
    RecompensaDao recDao = new RecompensaDao();
    Recompensa rec = new Recompensa("Recompensa teste " + suf, "benefício de teste", 150, 1);
    recDao.inserir(rec);
    LIMPEZA.add(() -> tenta(() -> recDao.excluir(rec.getId())));

    boolean recusou = false;
    try {
      service.resgatarRecompensa(ana, rec.getId());
    } catch (IllegalStateException esperado) {
      recusou = true;
      System.out.println("pontos insuficientes rejeitado -> " + esperado.getMessage());
    }
    if (!recusou) throw new IllegalStateException("Falha: resgate sem saldo deveria ser recusado");

    service.registrarAcao(ana, "Plantio de Árvore", 100);
    service.registrarAcao(ana, "Reciclagem", 30);
    System.out.println("saltou para " + ana.getPontos() + " pts");

    Recompensa resgatada = service.resgatarRecompensa(ana, rec.getId());
    if (resgatada.getEstoque() != 0 || ana.getPontos() != 10 || ana.getResgatados() != 150) {
      throw new IllegalStateException("Falha: resgate não aplicado corretamente");
    }
    Usuario anaBd = usuarioDao.buscarPorId(ana.getId());
    System.out.println("resgate -> " + resgatada.getTitulo()
      + " | estoque restante " + resgatada.getEstoque()
      + " | saldo " + anaBd.getPontos()
      + " | resgatados " + anaBd.getResgatados());

    recusou = false;
    try {
      service.resgatarRecompensa(ana, rec.getId());
    } catch (IllegalStateException esperado) {
      recusou = true;
      System.out.println("recompensa esgotada rejeitada -> " + esperado.getMessage());
    }
    if (!recusou) throw new IllegalStateException("Falha: resgate esgotado deveria ser recusado");
  }

  private static String nomes(List<Selo> selos) {
    return selos.stream().map(Selo::getNome).reduce((a, b) -> a + ", " + b).orElse("nenhum");
  }

  private static void limpar() {
    System.out.println("\n--- Limpeza dos dados criados no teste ---");
    for (int i = LIMPEZA.size() - 1; i >= 0; i--) {
      try {
        LIMPEZA.get(i).run();
      } catch (RuntimeException e) {
        System.out.println("Aviso na limpeza: " + e.getMessage());
      }
    }
    LIMPEZA.clear();
  }

  /** Remove, na ordem correta (filhos antes do pai), os dados do fluxo. */
  private static void limparFluxo(
    UsuarioDao usuarioDao,
    HistoricoDao historicoDao,
    UsuarioSeloDao usDao,
    SeloDao seloDao,
    Usuario ana,
    long idSeloInicio
  ) {
    tenta(() -> {
      for (Historico h : historicoDao.listarPorUsuario(ana.getId())) {
        historicoDao.excluir(h.getId());
      }
    });
    tenta(() -> {
      for (UsuarioSelo rel : usDao.listarPorUsuario(ana.getId())) {
        usDao.excluir(ana.getId(), rel.getSeloId());
      }
    });
    tenta(() -> usuarioDao.excluir(ana.getId()));
    tenta(() -> seloDao.excluir(idSeloInicio));
    System.out.println("fluxo de teste removido do banco");
  }

  private static void tenta(OperacaoBanco operacao) {
    try {
      operacao.executar();
    } catch (SQLException e) {
      System.out.println("Aviso na limpeza: " + e.getMessage());
    }
  }

  private interface OperacaoBanco {

    void executar() throws SQLException;
  }
}
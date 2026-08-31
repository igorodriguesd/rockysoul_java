package br.com.rockysoulup;

import br.com.rockysoulup.dao.*;
import br.com.rockysoulup.model.*;
import br.com.rockysoulup.service.RockySoulService;
import java.sql.SQLException;
import java.util.*;

/**
 * Suite de testes do CRUD completo: cadastra 10 usuários com pontos, níveis
 * e selos diferentes, valida a consistência do que foi gravado,
 * exercita Create/Read/Update/Delete e a exclusão em cascata, e limpa tudo.
 */
public final class TesteCrudCompleto {

  public static void main(String[] args) throws Exception {
    String suf = String.valueOf(System.currentTimeMillis());
    RockySoulService service = new RockySoulService();
    UsuarioDao usuarioDao = new UsuarioDao();
    HistoricoDao historicoDao = new HistoricoDao();
    SeloDao seloDao = new SeloDao();

    List<Selo> selosCriados = new ArrayList<>();
    List<Usuario> criados = new ArrayList<>();
    int totalAcoes = 0;

    try {
      System.out.println("===== TESTE CRUD COMPLETO (10 usuários) =====\n");

      System.out.println("--- Preparação: 3 selos ---");
      Selo bronze = new Selo("Bronze " + suf, "atingir 100 pts", 100);
      Selo prata = new Selo("Prata " + suf, "atingir 300 pts", 300);
      Selo ouro = new Selo("Ouro " + suf, "atingir 500 pts", 500);
      seloDao.inserir(bronze);
      seloDao.inserir(prata);
      seloDao.inserir(ouro);
      selosCriados.addAll(Arrays.asList(bronze, prata, ouro));

      int[][] acoes = {
        {30},
        {30, 30, 30},
        {50, 50},
        {50, 20, 20},
        {50, 50, 50, 50, 50, 20},
        {50, 50, 50, 50, 50, 50, 20},
        {50, 50, 50, 50, 50, 50, 50, 50, 50, 50},
        {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 20},
        {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50},
        {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 20, 20},
      };
      String[] niveisEsperados = {
        "SEMENTE", "SEMENTE", "BROTO", "SEMENTE", "BROTO",
        "ÁRVORE", "ÁRVORE", "ÁRVORE", "ÁRVORE", "ÁRVORE"
      };
      int[] selosEsperados = {0, 0, 1, 0, 1, 2, 3, 3, 3, 3};

      System.out.println("\n--- 1. Cadastro dos 10 usuários + ações + recompensas ---");
      for (int i = 0; i < 10; i++) {
        String nome = "User " + (i + 1) + " " + suf;
        String email = "user" + (i + 1) + "." + suf + "@email.com";
        Usuario u = service.cadastrarUsuario(nome, email);
        criados.add(u);
        for (int v : acoes[i]) {
          service.registrarAcao(u, "Acao teste " + v + " pts", v);
          totalAcoes++;
        }
        System.out.printf(
          "%02d. %-12s | pontos=%3d | %-6s | %d selo(s)%n",
          i + 1, nome, u.getPontos(), u.getNivel(),
          contarSelos(service, u, suf)
        );
      }
      boolean rejeitou = false;
      try {
        service.cadastrarUsuario("User 11 dup " + suf, "user1." + suf + "@email.com");
      } catch (IllegalStateException esperado) {
        rejeitou = true;
      }
      if (!rejeitou) {
        throw new IllegalStateException("Falha: e-mail duplicado deveria ser rejeitado");
      }
      System.out.println("Cadastro com e-mail repetido -> rejeitado");

      System.out.println("\n--- 2. Verificação: pontos, nível e selos conferem ---");
      int[] metas = {30, 90, 100, 90, 270, 320, 500, 520, 550, 590};
      for (int i = 0; i < 10; i++) {
        Usuario u = criados.get(i);
        if (u.getPontos() != metas[i]) {
          throw new IllegalStateException("Falha " + u.getNome() + ": pontos " + u.getPontos() + " != " + metas[i]);
        }
        if (!niveisEsperados[i].equals(u.getNivel())) {
          throw new IllegalStateException("Falha " + u.getNome() + ": nivel " + u.getNivel());
        }
        int selos = contarSelos(service, u, suf);
        if (selos != selosEsperados[i]) {
          throw new IllegalStateException("Falha " + u.getNome() + ": esperava " + selosEsperados[i] + " selos, veio " + selos);
        }
        Usuario doBanco = usuarioDao.buscarPorId(u.getId());
        if (doBanco == null || doBanco.getPontos() != metas[i] || !niveisEsperados[i].equals(doBanco.getNivel())) {
          throw new IllegalStateException("Falha " + u.getNome() + ": banco dessincronizado");
        }
      }
      System.out.println("10 usuários conferem no banco (pontos, nível, selos).");

      System.out.println("\n--- 3. Histórico ---");
      for (int i = 0; i < 10; i++) {
        Usuario u = criados.get(i);
        int esperado = acoes[i].length;
        if (historicoDao.listarPorUsuario(u.getId()).size() != esperado) {
          throw new IllegalStateException("Falha " + u.getNome() + ": historico incompleto");
        }
      }
      System.out.println("Históricos ok para os 10 usuários.");

      System.out.println("\n--- 4. Update (nome e e-mail) no User 02 ---");
      Usuario u2 = criados.get(1);
      u2.setNome("User 02 renomeado " + suf);
      u2.setEmail("renomeado." + suf + "@email.com");
      usuarioDao.atualizar(u2);
      if (!usuarioDao.buscarPorEmail(u2.getEmail()).getNome().equals(u2.getNome())) {
        throw new IllegalStateException("Falha: update nome/e-mail não persistiu");
      }
      System.out.println("Update nome+e-mail -> ok");

      System.out.println("\n--- 5. Update direto de pontos recalcula o nível ---");
      Usuario u1 = criados.get(0);
      u1.setPontos(250);
      usuarioDao.atualizar(u1);
      if (!"BROTO".equals(usuarioDao.buscarPorId(u1.getId()).getNivel())) {
        throw new IllegalStateException("Falha: nível deveria recalcular para BROTO");
      }
      u1.setPontos(30);
      usuarioDao.atualizar(u1);
      System.out.println("Pontos+recalculo de nível -> ok (250=BROTO, restaurado 30=SEMENTE)");

      System.out.println("\n--- 6. Ranking ordenado por pontos ---");
      List<Usuario> ranking = usuarioDao.listar();
      if (ranking.isEmpty() || ranking.get(0).getPontos() != 590) {
        throw new IllegalStateException("Falha: ranking deveria começar em 590 pts");
      }
      for (int j = 1; j < ranking.size(); j++) {
        if (ranking.get(j - 1).getPontos() < ranking.get(j).getPontos()) {
          throw new IllegalStateException("Falha: ranking fora de ordem");
        }
      }
      System.out.println("Top 3 ranking: " + ranking.get(0).getNome() + " (" + ranking.get(0).getPontos() + "), "
        + ranking.get(1).getNome() + " (" + ranking.get(1).getPontos() + "), "
        + ranking.get(2).getNome() + " (" + ranking.get(2).getPontos() + ")");

      System.out.println("\n--- 7. Delete usuário com dependentes (User 10) ---");
      Usuario u10 = criados.get(9);
      long id10 = u10.getId();
      service.excluirUsuario(id10);
      if (usuarioDao.buscarPorId(id10) != null
        || !historicoDao.listarPorUsuario(id10).isEmpty()
        || seloDao.buscarPorId(bronze.getId()) == null) {
        throw new IllegalStateException("Falha: cascata do usuário incompleta");
      }
      System.out.println("User 10 e seus dependentes removidos -> ok; selos/banco intactos.");

      System.out.println("\n--- 8. Delete selo com vínculos (cascata em serviço) ---");
      try {
        seloDao.excluir(bronze.getId());
        throw new IllegalStateException("Falha: pisada na FK deveria ter estourado ORA-02292");
      } catch (SQLException e) {
        System.out.println("Exclusão direta de selo com vínculos -> bloqueada pela FK (" + primeiroErro(e) + ")");
      }
      service.excluirSelo(bronze.getId());
      if (seloDao.buscarPorId(bronze.getId()) != null) {
        throw new IllegalStateException("Falha: cascata do selo incompleta");
      }
      System.out.println("service.excluirSelo -> removido com vínculos -> ok");

      System.out.println("\n===== TODOS OS CENÁRIOS DO CRUD COMPLETO PASSARAM =====\n");
    } finally {
      System.out.println("\n--- Limpeza total dos dados do teste ---");
      for (Usuario u : criados) {
        try {
          service.excluirUsuario(u.getId());
        } catch (Exception e) {
          System.out.println("Aviso limpeza usuário: " + e.getMessage());
        }
      }
      for (Selo s : selosCriados) {
        try {
          service.excluirSelo(s.getId());
        } catch (Exception e) {
          System.out.println("Aviso limpeza selo " + s.getId() + ": " + e.getMessage());
        }
      }
      System.out.println(
        "Resumo: " + criados.size() + " usuários, " + totalAcoes + " ações, "
          + "3 selos criados e removidos."
      );
    }
  }

  private static String primeiroErro(SQLException e) {
    return (e.getErrorCode() == 2292) ? "ORA-02292 (restrição de integridade)" : String.valueOf(e.getErrorCode());
  }

  /** Conta apenas os selos criados por esta suite (nome termina com o sufixo). */
  private static int contarSelos(RockySoulService service, Usuario u, String suf) {
    int total = 0;
    for (Selo s : service.listarSelosConcedidos(u)) {
      if (s.getNome().endsWith(suf)) total++;
    }
    return total;
  }
}
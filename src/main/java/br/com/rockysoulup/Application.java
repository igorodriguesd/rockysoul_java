package br.com.rockysoulup;

import br.com.rockysoulup.model.AcaoSustentavel;
import br.com.rockysoulup.model.Recompensa;
import br.com.rockysoulup.model.RegistroAcao;
import br.com.rockysoulup.model.Usuario;
import br.com.rockysoulup.repository.JsonDatabase;
import br.com.rockysoulup.service.GamificacaoService;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Application {

  private static final DateTimeFormatter FORMATO_DATA =
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
  private static final String SENHA_ADMIN = "senha";

  private final Scanner scanner = new Scanner(System.in);
  private final JsonDatabase db = new JsonDatabase();
  private final GamificacaoService gamificacao = new GamificacaoService();
  private Usuario usuarioLogado;

  public static void main(String[] args) {
    new Application().iniciar();
  }

  private void iniciar() {
    autenticar();
    while (true) {
      System.out.println("\n===== DASHBOARD SOULUP =====");
      System.out.printf(
        "[ Pontos Disponíveis: %d | Já Resgatados: %d | Nível: %s ]%n",
        usuarioLogado.getPontos(),
        usuarioLogado.getPontosResgatados(),
        usuarioLogado.getNivel()
      );
      System.out.println("-----------------------------------------------------------");
      System.out.println("1 - Registrar ação sustentável");
      System.out.println("2 - Ver Meu Nível e Estatísticas");
      System.out.println("3 - Resgatar Recompensas (Benefícios Reais)");
      System.out.println("4 - Sugestão do Avatar");
      System.out.println("5 - Ver Ranking da Semana");
      System.out.println("6 - Área do administrador");
      System.out.println("0 - Sair");
      int opcao = lerInteiro("Escolha: ");
      if (opcao == 0) {
        System.out.println(
          "Saindo... Seus dados de impacto foram salvos com segurança."
        );
        break;
      }
      try {
        executar(opcao);
      } catch (IllegalArgumentException | IllegalStateException e) {
        System.out.println("Erro: " + e.getMessage());
      } catch (RuntimeException e) {
        System.out.println("Erro inesperado: " + e.getMessage());
      }
    }
    scanner.close();
  }

  private void autenticar() {
    System.out.println("Base de dados: " + JsonDatabase.caminhoDoArquivo());
    while (true) {
      String nome = lerTexto("Nome: ");
      String email = lerTexto("E-mail: ");
      Usuario existente = db.buscarUsuarioPorEmail(email);
      if (existente != null) {
        usuarioLogado = existente;
        System.out.printf(
          "Bem-vindo de volta, %s!%n",
          usuarioLogado.getNome()
        );
        return;
      }
      if (db.buscarUsuarioPorNome(nome) != null) {
        System.out.println("Este nome já está em uso. Escolha outro.");
        continue;
      }
      try {
        usuarioLogado = new Usuario(nome, email);
        db.inserirUsuario(usuarioLogado);
        System.out.println("Cadastro criado com sucesso!");
        return;
      } catch (IllegalArgumentException | IllegalStateException e) {
        System.out.println("Erro: " + e.getMessage());
      }
    }
  }

  private void executar(int opcao) {
    switch (opcao) {
      case 1 -> registrarAcao();
      case 2 -> verNivelEstatisticas();
      case 3 -> resgatarRecompensa();
      case 4 -> sugestaoAvatar();
      case 5 -> verRanking();
      case 6 -> areaAdmin();
      default -> System.out.println("Opção inválida! Digite de 1 a 6.");
    }
  }

  private void registrarAcao() {
    List<AcaoSustentavel> acoes = db.listarAcoes();
    if (acoes.isEmpty()) {
      System.out.println("Nenhuma ação cadastrada.");
      return;
    }
    System.out.println("\nAções Sustentáveis disponíveis:");
    for (int i = 0; i < acoes.size(); i++) {
      AcaoSustentavel a = acoes.get(i);
      System.out.printf("%d - %s%n", i + 1, a.getNome());
    }
    int posicao = lerInteiro("\nQual ação sustentável você realizou hoje? ");
    if (posicao < 1 || posicao > acoes.size()) {
      System.out.println("Opção inválida!");
      return;
    }
    AcaoSustentavel acao = acoes.get(posicao - 1);
    gamificacao.registrarAcao(usuarioLogado, acao);
    db.inserirRegistro(new RegistroAcao(usuarioLogado, acao));
    db.atualizarUsuario(usuarioLogado);
    System.out.printf(
      "Boa! Você ganhou +%d Pontos ECOA com: %s!%n",
      acao.getPontos(),
      acao.getNome()
    );
  }

  private void verNivelEstatisticas() {
    String nivel = usuarioLogado.calcularNivel();
    System.out.printf("%n--- SEU NÍVEL ATUAL: %s ---%n", nivel);
    System.out.printf(
      "Saldo Disponível: %d Pontos ECOA%n",
      usuarioLogado.getPontos()
    );
    System.out.printf(
      "Total já Resgatado: %d Pontos ECOA%n",
      usuarioLogado.getPontosResgatados()
    );

    List<RegistroAcao> registros = db.listarRegistrosPorUsuario(
      usuarioLogado.getId()
    );
    if (registros.isEmpty()) {
      System.out.println("Histórico de Missões: vazio. Comece hoje!");
    } else {
      Map<String, Integer> quantidade = new LinkedHashMap<>();
      Map<String, Integer> pontos = new LinkedHashMap<>();
      for (RegistroAcao r : registros) {
        String nome =
          r.getAcao() == null ? "(ação removida)" : r.getAcao().getNome();
        quantidade.merge(nome, 1, Integer::sum);
        pontos.merge(nome, r.getPontosObtidos(), Integer::sum);
      }
      System.out.println("Histórico de Missões:");
      for (Map.Entry<String, Integer> e : quantidade.entrySet()) {
        System.out.printf(
          "  • %s x%d (%d pts)%n",
          e.getKey(),
          e.getValue(),
          pontos.get(e.getKey())
        );
      }
    }

    String proximo = usuarioLogado.proximoNivel();
    if (proximo == null) {
      System.out.println("Você alcançou o nível máximo! Você é lenda! 🏆");
    } else {
      System.out.printf(
        "Progresso: Faltam %d pontos para evoluir para %s!%n",
        usuarioLogado.pontosParaProximoNivel(),
        proximo
      );
    }
  }

  private void resgatarRecompensa() {
    List<Recompensa> recompensas = db.listarRecompensas();
    if (recompensas.isEmpty()) {
      System.out.println("Nenhuma recompensa cadastrada.");
      return;
    }
    System.out.println("\n=== VITRINE DE RECOMPENSAS SOULUP ===");
    for (int i = 0; i < recompensas.size(); i++) {
      Recompensa r = recompensas.get(i);
      String estoque =
        r.getEstoque() > 0 ? "estoque " + r.getEstoque() : "ESGOTADO";
      System.out.printf(
        "%d - %s -------------- %d pts (%s)%n",
        i + 1,
        r.getTitulo(),
        r.getCusto(),
        estoque
      );
    }
    System.out.println("0 - Voltar");
    int opcao = lerInteiro(
      "\nSeu Saldo: " +
      usuarioLogado.getPontos() +
      " pts. Escolha um benefício: "
    );
    if (opcao == 0) return;
    if (opcao < 1 || opcao > recompensas.size()) {
      System.out.println("Opção inválida!");
      return;
    }
    Recompensa escolhida = recompensas.get(opcao - 1);
    try {
      gamificacao.resgatar(usuarioLogado, escolhida);
      db.atualizarUsuario(usuarioLogado);
      db.atualizarRecompensa(escolhida);
      System.out.printf(
        "%nSucesso! Você resgatou: %s!%n",
        escolhida.getTitulo()
      );
      System.out.printf(
        "Foram utilizados %d Pontos ECOA. Saldo atual: %d pts.%n",
        escolhida.getCusto(),
        usuarioLogado.getPontos()
      );
    } catch (IllegalStateException e) {
      System.out.printf(
        "%nPontos insuficientes ou estoque esgotado! Você precisa de %d pontos, mas tem apenas %d.%n",
        escolhida.getCusto(),
        usuarioLogado.getPontos()
      );
    }
  }

  private void sugestaoAvatar() {
    int pontos = usuarioLogado.getPontos();
    String proximo = usuarioLogado.proximoNivel();
    System.out.println("\n--- MENSAGEM DO AVATAR ---");
    if (pontos < 100) {
      System.out.println(
        "Avatar: Que ótimo ver você por aqui! Registre missões diárias para fazermos nossa semente brotar."
      );
    } else if (pontos < 400) {
      System.out.println(
        "Avatar: Crescendo firme! Continue registrando ações para desbloquear as melhores recompensas."
      );
    } else if (proximo != null) {
      System.out.printf(
        "Avatar: Parabéns pelo nível %s! Faltam só %d pontos para virar %s.%n",
        usuarioLogado.getNivel(),
        usuarioLogado.pontosParaProximoNivel(),
        proximo
      );
    } else {
      System.out.println(
        "Avatar: Você é JARDINEIRO DO EDEN! O planeta agradece, lenda! 🌍"
      );
    }
  }

  private void verRanking() {
    List<Usuario> usuarios = db.listarUsuarios();
    usuarios.sort(Comparator.comparingInt(Usuario::getPontos).reversed());
    System.out.println("\n==============================");
    System.out.println("      RANKING DA SEMANA       ");
    System.out.println("==============================");
    int posicao = 1;
    for (Usuario u : usuarios) {
      boolean souEu = u.getId().equals(usuarioLogado.getId());
      if (souEu) {
        System.out.printf(
          "-> %dº %-15s | %d pts * (Sua posição)%n",
          posicao,
          u.getNome(),
          u.getPontos()
        );
      } else {
        System.out.printf(
          "   %dº %-15s | %d pts%n",
          posicao,
          u.getNome(),
          u.getPontos()
        );
      }
      posicao++;
    }
    System.out.println("==============================");
  }

  private void areaAdmin() {
    String senha = lerTexto("Senha do administrador: ");
    if (!SENHA_ADMIN.equals(senha)) {
      System.out.println("Senha incorreta.");
      return;
    }
    while (true) {
      System.out.println("\n--- ÁREA DO ADMINISTRADOR ---");
      System.out.println("1 - Usuários");
      System.out.println("2 - Ações sustentáveis");
      System.out.println("3 - Recompensas");
      System.out.println("0 - Voltar");
      int opcao = lerInteiro("Escolha: ");
      if (opcao == 0) return;
      switch (opcao) {
        case 1 -> menuUsuarios();
        case 2 -> menuAcoes();
        case 3 -> menuRecompensas();
        default -> System.out.println("Opção inválida.");
      }
    }
  }

  private void menuUsuarios() {
    while (true) {
      System.out.println("\n--- USUÁRIOS ---");
      System.out.println("1 - Cadastrar");
      System.out.println("2 - Listar");
      System.out.println("3 - Atualizar");
      System.out.println("4 - Excluir");
      System.out.println("0 - Voltar");
      int opcao = lerInteiro("Escolha: ");
      if (opcao == 0) return;
      switch (opcao) {
        case 1 -> cadastrarUsuario();
        case 2 -> listarUsuarios();
        case 3 -> atualizarUsuario();
        case 4 -> excluirUsuario();
        default -> System.out.println("Opção inválida.");
      }
    }
  }

  private void cadastrarUsuario() {
    Usuario usuario = new Usuario(
      lerTexto("Nome: "),
      lerTexto("E-mail: ")
    );
    db.inserirUsuario(usuario);
    System.out.println("Usuário cadastrado com id " + usuario.getId());
  }

  private void listarUsuarios() {
    List<Usuario> usuarios = db.listarUsuarios();
    if (usuarios.isEmpty()) {
      System.out.println("Nenhum usuário cadastrado.");
      return;
    }
    for (Usuario u : usuarios) {
      System.out.printf(
        "%d - %s | %s | %d pts | %d resgatados | %s%n",
        u.getId(),
        u.getNome(),
        u.getEmail(),
        u.getPontos(),
        u.getPontosResgatados(),
        u.getNivel()
      );
    }
  }

  private void atualizarUsuario() {
    Usuario usuario = db.buscarUsuarioPorId(lerInteiro("ID do usuário: "));
    if (usuario == null) {
      System.out.println("Usuário não encontrado.");
      return;
    }
    usuario.setNome(
      lerTextoOuPadrao(
        "Nome [" + usuario.getNome() + "]: ",
        usuario.getNome()
      )
    );
    usuario.setEmail(
      lerTextoOuPadrao(
        "E-mail [" + usuario.getEmail() + "]: ",
        usuario.getEmail()
      )
    );
    db.atualizarUsuario(usuario);
    System.out.println("Usuário atualizado.");
  }

  private void excluirUsuario() {
    long id = lerInteiro("ID do usuário: ");
    db.excluirUsuario(id);
    System.out.println("Usuário excluído (se existia).");
  }

  private void menuAcoes() {
    while (true) {
      System.out.println("\n--- AÇÕES SUSTENTÁVEIS ---");
      System.out.println("1 - Cadastrar");
      System.out.println("2 - Listar");
      System.out.println("3 - Atualizar");
      System.out.println("4 - Excluir");
      System.out.println("0 - Voltar");
      int opcao = lerInteiro("Escolha: ");
      if (opcao == 0) return;
      switch (opcao) {
        case 1 -> cadastrarAcao();
        case 2 -> listarAcoes();
        case 3 -> atualizarAcao();
        case 4 -> excluirAcao();
        default -> System.out.println("Opção inválida.");
      }
    }
  }

  private void cadastrarAcao() {
    AcaoSustentavel acao = new AcaoSustentavel(
      lerTexto("Nome: "),
      lerTexto("Descrição: "),
      lerInteiro("Pontos: ")
    );
    db.inserirAcao(acao);
    System.out.println("Ação cadastrada.");
  }

  private void listarAcoes() {
    List<AcaoSustentavel> acoes = db.listarAcoes();
    if (acoes.isEmpty()) {
      System.out.println("Nenhuma ação cadastrada.");
      return;
    }
    for (AcaoSustentavel a : acoes) {
      System.out.printf(
        "%d - %s | %s | %d pts%n",
        a.getId(),
        a.getNome(),
        a.getDescricao(),
        a.getPontos()
      );
    }
  }

  private void atualizarAcao() {
    AcaoSustentavel acao = db.buscarAcaoPorId(lerInteiro("ID da ação: "));
    if (acao == null) {
      System.out.println("Ação não encontrada.");
      return;
    }
    acao.setNome(
      lerTextoOuPadrao("Nome [" + acao.getNome() + "]: ", acao.getNome())
    );
    acao.setDescricao(lerTextoOuPadrao("Descrição: ", acao.getDescricao()));
    acao.setPontos(lerInteiro("Pontos: "));
    db.atualizarAcao(acao);
    System.out.println("Ação atualizada.");
  }

  private void excluirAcao() {
    long id = lerInteiro("ID da ação: ");
    db.excluirAcao(id);
    System.out.println("Ação excluída (se existia).");
  }

  private void menuRecompensas() {
    while (true) {
      System.out.println("\n--- RECOMPENSAS ---");
      System.out.println("1 - Cadastrar");
      System.out.println("2 - Listar");
      System.out.println("3 - Atualizar");
      System.out.println("4 - Excluir");
      System.out.println("0 - Voltar");
      int opcao = lerInteiro("Escolha: ");
      if (opcao == 0) return;
      switch (opcao) {
        case 1 -> cadastrarRecompensa();
        case 2 -> listarRecompensas();
        case 3 -> atualizarRecompensa();
        case 4 -> excluirRecompensa();
        default -> System.out.println("Opção inválida.");
      }
    }
  }

  private void cadastrarRecompensa() {
    Recompensa recompensa = new Recompensa(
      lerTexto("Título: "),
      lerTexto("Descrição: "),
      lerInteiro("Custo em pontos: "),
      lerInteiro("Estoque: ")
    );
    db.inserirRecompensa(recompensa);
    System.out.println("Recompensa cadastrada.");
  }

  private void listarRecompensas() {
    List<Recompensa> recompensas = db.listarRecompensas();
    if (recompensas.isEmpty()) {
      System.out.println("Nenhuma recompensa cadastrada.");
      return;
    }
    for (Recompensa r : recompensas) {
      System.out.printf(
        "%d - %s | %s | %d pts | estoque %d%n",
        r.getId(),
        r.getTitulo(),
        r.getDescricao(),
        r.getCusto(),
        r.getEstoque()
      );
    }
  }

  private void atualizarRecompensa() {
    Recompensa recompensa =
      db.buscarRecompensaPorId(lerInteiro("ID da recompensa: "));
    if (recompensa == null) {
      System.out.println("Recompensa não encontrada.");
      return;
    }
    recompensa.setTitulo(
      lerTextoOuPadrao(
        "Título [" + recompensa.getTitulo() + "]: ",
        recompensa.getTitulo()
      )
    );
    recompensa.setDescricao(
      lerTextoOuPadrao("Descrição: ", recompensa.getDescricao())
    );
    recompensa.setCusto(lerInteiro("Custo em pontos: "));
    recompensa.setEstoque(lerInteiro("Estoque: "));
    db.atualizarRecompensa(recompensa);
    System.out.println("Recompensa atualizada.");
  }

  private void excluirRecompensa() {
    long id = lerInteiro("ID da recompensa: ");
    db.excluirRecompensa(id);
    System.out.println("Recompensa excluída (se existia).");
  }

  private int lerInteiro(String rotulo) {
    while (true) {
      System.out.print(rotulo);
      String valor = scanner.nextLine().trim();
      try {
        return Integer.parseInt(valor);
      } catch (NumberFormatException e) {
        System.out.println("Digite um número válido.");
      }
    }
  }

  private String lerTexto(String rotulo) {
    while (true) {
      System.out.print(rotulo);
      String valor = scanner.nextLine().trim();
      if (!valor.isEmpty()) return valor;
      System.out.println("Valor obrigatório.");
    }
  }

  private String lerTextoOuPadrao(String rotulo, String padrao) {
    System.out.print(rotulo);
    String valor = scanner.nextLine().trim();
    return valor.isEmpty() ? padrao : valor;
  }
}

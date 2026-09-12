package br.com.rockysoulup;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.exception.RegistroDuplicadoException;
import br.com.rockysoulup.exception.SaldoInsuficienteException;
import br.com.rockysoulup.model.*;
import br.com.rockysoulup.service.RockySoulService;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Application {

  private final Scanner scanner = new Scanner(System.in);
  private final RockySoulService service = new RockySoulService();
  private Usuario usuarioLogado;

  public static void main(String[] args) {
    try {
      new Application().iniciar();
    } catch (RuntimeException e) {
      System.err.println("\nErro inesperado no sistema: " + e.getMessage());
      System.err.println("Consulte o log para mais detalhes.");
      System.exit(1);
    }
  }

  private void iniciar() {
    service.garantirCatalogo();
    System.out.println("=== SISTEMA DE GAMIFICAÇÃO SUSTENTÁVEL SOULUP ===");
    while (true) {
      System.out.println("\n===== MENU PRINCIPAL =====");
      System.out.println("1 - Uso do sistema");
      System.out.println("2 - Área de cadastro (CRUD)");
      System.out.println("0 - Sair");
      int opcao = lerInteiro("Escolha: ");
      if (opcao == 0) {
        System.out.println("Até logo! Continue salvando o planeta!");
        break;
      }
      if (opcao == 1) {
        usoDoSistema();
      } else if (opcao == 2) {
        try {
          areaAdmin();
        } catch (RuntimeException e) {
          System.out.println("Erro inesperado: " + e.getMessage());
        }
      } else {
        System.out.println("Opção inválida!");
      }
    }
    scanner.close();
  }

  private void usoDoSistema() {
    autenticar();
    while (true) {
      System.out.printf(
        "%n[ Pontos: %d | Resgatados: %d | Nível: %s ]%n",
        usuarioLogado.getPontos(),
        usuarioLogado.getResgatados(),
        usuarioLogado.getNivel()
      );
      System.out.println("\n===== DASHBOARD SOULUP =====");
      System.out.println("1 - Registrar ação sustentável");
      System.out.println("2 - Ver meu nível e estatísticas");
      System.out.println("3 - Resgatar recompensas (benefícios reais)");
      System.out.println("4 - Minha coleção de cartas");
      System.out.println("0 - Voltar");
      int opcao = lerInteiro("Escolha: ");
      if (opcao == 0) {
        System.out.println("Voltando ao menu principal...");
        return;
      }
      try {
        executar(opcao);
      } catch (IllegalArgumentException | IllegalStateException e) {
        System.out.println("Erro: " + e.getMessage());
      } catch (RuntimeException e) {
        System.out.println("Erro inesperado: " + e.getMessage());
      }
    }
  }

  private void autenticar() {
    System.out.println("Base de dados: " + ConnectionFactory.endereco());
    while (true) {
      String nome = lerTexto("Nome: ");
      String email = lerTexto("E-mail: ");
      try {
        Usuario existente = service.usuarios().buscarPorEmail(email);
        if (existente != null) {
          usuarioLogado = existente;
          System.out.println("Bem-vindo de volta, " + existente.getNome() + "!");
          return;
        }
        usuarioLogado = service.cadastrarUsuario(nome, email);
        System.out.println("Cadastro criado com sucesso!");
        return;
      } catch (RegistroDuplicadoException e) {
        System.out.println("Erro: " + e.getMessage());
      } catch (IllegalArgumentException | IllegalStateException e) {
        System.out.println("Erro: " + e.getMessage());
      } catch (SQLException e) {
        System.out.println("Falha ao acessar o banco: " + e.getMessage());
      }
    }
  }

  private void executar(int opcao) {
    switch (opcao) {
      case 1 -> registrarAcao();
      case 2 -> verNivelEstatisticas();
      case 3 -> resgatarRecompensas();
      case 4 -> verMinhaColecao();
      default -> System.out.println("Opção inválida! Digite de 1 a 4.");
    }
  }

  private void registrarAcao() {
    List<Acao> acoes = service.listarAcoes();
    if (acoes.isEmpty()) {
      System.out.println("Nenhuma ação cadastrada.");
      return;
    }
    System.out.println("\nAções Sustentáveis disponíveis:");
    for (int i = 0; i < acoes.size(); i++) {
      System.out.printf("%d - %s (%d pts)%n", i + 1, acoes.get(i).getNome(), acoes.get(i).getPontos());
    }
    int posicao = lerInteiro("\nQual ação sustentável você realizou hoje? ");
    if (posicao < 1 || posicao > acoes.size()) {
      System.out.println("Erro: opção inválida!");
      return;
    }
    Acao acao = acoes.get(posicao - 1);
    RockySoulService.ResultadoAcao resultado = service.registrarAcao(usuarioLogado, acao.getNome(), acao.getPontos());
    System.out.printf("Boa! Você ganhou +%d Pontos ECOA com: %s!%n", acao.getPontos(), acao.getNome());
    anunciarSelosNovos(resultado.selosConquistados());
    resultado.cartaDrop().ifPresent(this::anunciarSorteio);
  }

  private void anunciarSorteio(RockySoulService.ResultadoSorteio sorteio) {
    if (!sorteio.caiu()) {
      System.out.println("Você tentou coletar uma carta ecológica, mas desta vez não caiu nenhuma. Tente de novo!");
      return;
    }
    Carta carta = sorteio.carta();
    if (sorteio.nova()) {
      System.out.printf(
        "Nova carta coletada: %s (%s - %s)!%n",
        carta.getNome(), carta.getConjunto(), carta.getRaridade());
    } else {
      System.out.printf(
        "Carta repetida: %s (%s)! Você ganhou +%d fragmento(s) de raridade %s.%n",
        carta.getNome(), carta.getRaridade(), sorteio.fragmentosGanhos(), carta.getRaridade());
    }
  }

  private void verMinhaColecao() {
    List<Carta> minhas = service.listarCartasDoUsuario(usuarioLogado);
    Map<String, Integer> porSet = new LinkedHashMap<>();
    for (Carta c : minhas) porSet.merge(c.getConjunto(), 1, Integer::sum);

    System.out.println("\n=== MINHA COLEÇÃO DE CARTAS ===");
    if (minhas.isEmpty()) {
      System.out.println("Você ainda não tem cartas. Realize ações sustentáveis para ganhar cartas!");
    } else {
      System.out.printf("Total: %d de 20 cartas%n", minhas.size());
      for (Map.Entry<String, Integer> e : porSet.entrySet()) {
        System.out.printf("  • %s: %d/%d%n", e.getKey(), e.getValue(), 4);
      }
    }

    Map<String, Integer> fragmentos = service.fragmentosDoUsuario(usuarioLogado);
    System.out.println("\nFragmentos (para fabricar cartas):");
    boolean temFragmentos = false;
    for (String raridade : List.of("COMUM", "INCOMUM", "RARA", "EPICA", "LENDARIA")) {
      int quantidade = fragmentos.getOrDefault(raridade, 0);
      if (quantidade > 0) {
        System.out.printf("  • %s: %d%n", raridade, quantidade);
        temFragmentos = true;
      }
    }
    if (!temFragmentos) System.out.println("  Nenhum fragmento ainda. Cartas repetidas geram fragmentos.");

    fabricarCartaPorFragmentos();
  }

  private void fabricarCartaPorFragmentos() {
    System.out.println("\nDeseja fabricar uma carta com fragmentos?");
    System.out.println("1 - Sim");
    System.out.println("0 - Voltar");
    int escolha = lerInteiro("Escolha: ");
    if (escolha != 1) {
      System.out.println("Voltando...");
      return;
    }
    List<Carta> catalogadas = service.listarCartas();
    List<Carta> minhas = service.listarCartasDoUsuario(usuarioLogado);
    List<Carta> fabricaveis = new ArrayList<>();
    for (Carta c : catalogadas) {
      if (!minhas.stream().anyMatch(m -> m.getId().equals(c.getId()))) {
        fabricaveis.add(c);
      }
    }
    if (fabricaveis.isEmpty()) {
      System.out.println("Você já tem todas as cartas do catálogo!");
      return;
    }
    System.out.println("\nCartas que você ainda não possui:");
    for (int i = 0; i < fabricaveis.size(); i++) {
      Carta c = fabricaveis.get(i);
      System.out.printf(
        "%d - %s (%s - %s) | custo: %d fragmentos %s%n",
        i + 1, c.getNome(), c.getConjunto(), c.getRaridade(),
        RockySoulService.custoFabricacao(c.getRaridade()), c.getRaridade());
    }
    int escolhaCarta = lerInteiro("\nQual carta fabricar (0 cancela): ");
    if (escolhaCarta <= 0 || escolhaCarta > fabricaveis.size()) {
      System.out.println("Fabricação cancelada.");
      return;
    }
    try {
      Carta carta = service.fabricarCarta(usuarioLogado, fabricaveis.get(escolhaCarta - 1).getId());
      System.out.printf("Carta fabricada: %s (%s - %s)!%n",
        carta.getNome(), carta.getConjunto(), carta.getRaridade());
    } catch (SaldoInsuficienteException | IllegalStateException e) {
      System.out.println("Erro: " + e.getMessage());
    }
  }

  private void verNivelEstatisticas() {
    System.out.printf("%n--- SEU NÍVEL ATUAL: %s ---%n", usuarioLogado.getNivel());
    System.out.printf("Saldo Disponível: %d Pontos ECOA%n", usuarioLogado.getPontos());
    System.out.printf("Total já Resgatado: %d Pontos ECOA%n", usuarioLogado.getResgatados());

    List<Historico> registros = listarHistorico();
    if (registros.isEmpty()) {
      System.out.println("Histórico de Ações: vazio. Comece hoje!");
    } else {
      Map<String, Integer> quantidade = new LinkedHashMap<>();
      Map<String, Integer> pontos = new LinkedHashMap<>();
      for (Historico h : registros) {
        quantidade.merge(h.getDescricao(), 1, Integer::sum);
        pontos.merge(h.getDescricao(), h.getPontos(), Integer::sum);
      }
      System.out.println("Histórico de Ações:");
      for (Map.Entry<String, Integer> e : quantidade.entrySet()) {
        System.out.printf("  • %s x%d (+%d pts)%n", e.getKey(), e.getValue(), pontos.get(e.getKey()));
      }
    }

    List<Selo> selos = service.listarSelosConcedidos(usuarioLogado);
    System.out.println(
      selos.isEmpty()
        ? "Selos conquistados: nenhum ainda."
        : "Selos conquistados: " + nomes(selos)
    );

    String proximo = usuarioLogado.proximoNivel();
    if (proximo == null) {
      System.out.println("Você alcançou o nível máximo! Você é lenda!");
    } else {
      System.out.printf(
        "Progresso: Faltam %d pontos para evoluir para %s!%n",
        usuarioLogado.pontosParaProximoNivel(),
        proximo
      );
    }
  }

  private void resgatarRecompensas() {
    List<Recompensa> recompensas = service.listarRecompensas();
    if (recompensas.isEmpty()) {
      System.out.println("\nNenhuma recompensa cadastrada.");
      return;
    }
    System.out.println("\n=== VITRINE DE RECOMPENSAS SOULUP ===");
    for (int i = 0; i < recompensas.size(); i++) {
      Recompensa r = recompensas.get(i);
      String situacao = r.getEstoque() > 0 ? "estoque " + r.getEstoque() : "ESGOTADO";
      String badge = r.getDestaque().isEmpty() ? "" : " [" + r.getDestaque() + "]";
      String categoria = r.getCategoria().isEmpty() ? "" : " (" + r.getCategoria() + ")";
      System.out.printf("%d - %s%s%s ---- %d pts (%s)%n", i + 1, r.getTitulo(), badge, categoria, r.getCusto(), situacao);
    }
    System.out.println("0 - Voltar");
    int escolha = lerInteiro("\nSeu Saldo: " + usuarioLogado.getPontos() + " pts. Escolha um benefício: ");
    if (escolha == 0) {
      System.out.println("Operação cancelada.");
      return;
    }
    if (escolha < 0 || escolha > recompensas.size()) {
      System.out.println("Erro: opção inválida!");
      return;
    }
    Recompensa recompensa;
    try {
      recompensa = service.resgatarRecompensa(usuarioLogado, recompensas.get(escolha - 1).getId());
      System.out.println("\nSucesso! Você resgatou: " + recompensa.getTitulo() + "!");
      System.out.printf("Foram utilizados %d Pontos ECOA.%n", recompensa.getCusto());
    } catch (SaldoInsuficienteException e) {
      System.out.println("Erro: " + e.getMessage());
    }
  }

  /* ─────────────── ÁREA DE CADASTRO (CRUD) ─────────────── */

  private void areaAdmin() {
    while (true) {
      System.out.println("\n===== ÁREA DE CADASTRO (CRUD) =====");
      System.out.println("1 - Gerenciar usuários");
      System.out.println("2 - Gerenciar ações");
      System.out.println("3 - Gerenciar recompensas");
      System.out.println("4 - Gerenciar cartas");
      System.out.println("0 - Voltar");
      int opcao = lerInteiro("Escolha: ");
      if (opcao == 0) return;
      switch (opcao) {
        case 1 -> menuUsuarios();
        case 2 -> menuAcoes();
        case 3 -> menuRecompensas();
        case 4 -> menuCartas();
        default -> System.out.println("Opção inválida.");
      }
    }
  }

  private void menuUsuarios() {
    while (true) {
      System.out.println("\n===== GERENCIAR USUÁRIOS =====");
      System.out.println("1 - Incluir usuário");
      System.out.println("2 - Alterar usuário");
      System.out.println("3 - Excluir usuário");
      System.out.println("4 - Listar usuários");
      System.out.println("0 - Voltar");
      int opcao = lerInteiro("Escolha: ");
      if (opcao == 0) return;
      switch (opcao) {
        case 1 -> executarSafely(this::cadastrarUsuario);
        case 2 -> executarSafely(this::atualizarUsuario);
        case 3 -> executarSafely(this::excluirUsuario);
        case 4 -> executarSafely(this::imprimirUsuarios);
        default -> System.out.println("Opção inválida.");
      }
    }
  }

  private void cadastrarUsuario() {
    try {
      Usuario novo = service.cadastrarUsuario(
        lerTexto("Nome: "),
        lerTexto("E-mail: ")
      );
      System.out.println("Usuário '" + novo.getNome() + "' cadastrado com sucesso!");
    } catch (RegistroDuplicadoException e) {
      System.out.println("Erro: " + e.getMessage());
    } catch (IllegalArgumentException | IllegalStateException e) {
      System.out.println("Erro: " + e.getMessage());
    }
  }

  private void atualizarUsuario() {
    try {
      Usuario usuario = service.usuarios().buscarPorId(lerId("ID do usuário: "));
      if (usuario == null) {
        System.out.println("Erro: usuário não encontrado!");
        return;
      }
      String novoNome = lerTextoOuPadrao("Novo nome [" + usuario.getNome() + "]: ", usuario.getNome()).trim();
      String novoEmail = lerTextoOuPadrao("Novo e-mail [" + usuario.getEmail() + "]: ", usuario.getEmail()).trim();
      try {
        usuario.setNome(novoNome);
        usuario.setEmail(novoEmail);
      } catch (IllegalArgumentException e) {
        System.out.println("Erro: " + e.getMessage());
        return;
      }
      service.usuarios().atualizar(usuario);
      System.out.println("Usuário alterado com sucesso!");
    } catch (IllegalArgumentException | IllegalStateException | SQLException e) {
      System.out.println("Erro: " + e.getMessage());
    }
  }

  private void excluirUsuario() {
    long id = lerId("ID do usuário: ");
    try {
      service.excluirUsuario(id);
      System.out.println("Usuário excluído com sucesso!");
    } catch (IllegalStateException | SQLException e) {
      System.out.println("Erro: " + e.getMessage());
    }
  }

  private void imprimirUsuarios() {
    List<Usuario> usuarios = listarUsuarios();
    if (usuarios.isEmpty()) {
      System.out.println("Nenhum usuário cadastrado.");
      return;
    }
    for (Usuario u : usuarios) {
      System.out.printf(
        "%d - %s | %s | %d pts | %d resgatado(s) | nível %s%n",
        u.getId(), u.getNome(), u.getEmail(), u.getPontos(), u.getResgatados(), u.getNivel()
      );
    }
  }

  private void menuAcoes() {
    while (true) {
      System.out.println("\n===== GERENCIAR AÇÕES =====");
      System.out.println("1 - Incluir ação");
      System.out.println("2 - Alterar ação");
      System.out.println("3 - Excluir ação");
      System.out.println("4 - Listar ações");
      System.out.println("5 - Filtrar ações por pontos");
      System.out.println("0 - Voltar");
      int opcao = lerInteiro("Escolha: ");
      if (opcao == 0) return;
      switch (opcao) {
        case 1 -> executarSafely(this::incluirAcao);
        case 2 -> executarSafely(this::alterarAcao);
        case 3 -> executarSafely(this::excluirAcao);
        case 4 -> executarSafely(this::listarAcoesCrud);
        case 5 -> executarSafely(this::filtrarAcoes);
        default -> System.out.println("Opção inválida.");
      }
    }
  }

  private void incluirAcao() {
    try {
      String nome = lerTexto("Nome da ação: ");
      int pontos = lerInteiro("Pontos da ação: ");
      String erro = "";
      if (nome.isBlank()) {
        erro = "O nome é obrigatório!";
      } else if (pontos <= 0 || pontos > 100) {
        erro = "Os pontos devem estar entre 1 e 100!";
      } else if (service.acoes().buscarPorNome(nome) != null) {
        erro = "Já existe uma ação com este nome!";
      }
      if (!erro.isEmpty()) {
        System.out.println("Erro: " + erro);
        return;
      }
      Acao acao = new Acao(nome, pontos);
      service.acoes().inserir(acao);
      System.out.println("Ação '" + acao.getNome() + "' cadastrada com sucesso!");
    } catch (IllegalArgumentException | IllegalStateException | SQLException e) {
      System.out.println("Erro: " + e.getMessage());
    }
  }

  private void alterarAcao() {
    try {
      System.out.println("\nAções cadastradas:");
      listarAcoesCrud();
      List<Acao> acoes = service.listarAcoes();
      int escolha = lerInteiro("\nNúmero da ação a alterar (0 cancela): ");
      if (escolha == 0) {
        System.out.println("Alteração cancelada.");
        return;
      }
      if (escolha < 0 || escolha > acoes.size()) {
        System.out.println("Erro: opção inválida!");
        return;
      }
      Acao acao = acoes.get(escolha - 1);
      String novoNome = lerTextoOuPadrao("Novo nome [" + acao.getNome() + "]: ", acao.getNome());
      int novosPontos = lerInteiro("Novos pontos: ");
      if (novosPontos <= 0 || novosPontos > 100) {
        System.out.println("Erro: os pontos devem estar entre 1 e 100!");
        return;
      }
      acao.setNome(novoNome);
      acao.setPontos(novosPontos);
      service.acoes().atualizar(acao);
      System.out.println("Ação alterada com sucesso!");
    } catch (IllegalArgumentException | IllegalStateException | SQLException e) {
      System.out.println("Erro: " + e.getMessage());
    }
  }

  private void excluirAcao() {
    try {
      System.out.println("\nAções cadastradas:");
      List<Acao> acoes = service.listarAcoes();
      if (acoes.isEmpty()) {
        System.out.println("Nenhuma ação cadastrada.");
        return;
      }
      listarAcoesCrud();
      int escolha = lerInteiro("\nNúmero da ação a excluir (0 cancela): ");
      if (escolha == 0) {
        System.out.println("Exclusão cancelada.");
        return;
      }
      if (escolha < 0 || escolha > acoes.size()) {
        System.out.println("Erro: opção inválida!");
        return;
      }
      service.excluirAcao(acoes.get(escolha - 1).getId());
      System.out.println("Ação excluída com sucesso!");
    } catch (SQLException e) {
      System.out.println("Erro: " + e.getMessage());
    }
  }

  private void listarAcoesCrud() {
    List<Acao> acoes = service.listarAcoes();
    if (acoes.isEmpty()) {
      System.out.println("Nenhuma ação cadastrada.");
      return;
    }
    for (int i = 0; i < acoes.size(); i++) {
      Acao a = acoes.get(i);
      System.out.printf("%d - %s (%d pts)%n", i + 1, a.getNome(), a.getPontos());
    }
  }

  private void filtrarAcoes() {
    int minimo = lerInteiro("Filtrar ações com quantos pontos ou mais? ");
    if (minimo < 0) {
      System.out.println("Erro: o mínimo não pode ser negativo!");
      return;
    }
    List<Acao> resultado = new ArrayList<>();
    for (Acao a : service.listarAcoes()) {
      if (a.getPontos() >= minimo) resultado.add(a);
    }
    if (resultado.isEmpty()) {
      System.out.println("Nenhuma ação encontrada nesse filtro.");
      return;
    }
    for (Acao a : resultado) {
      System.out.printf("• %s (%d pts)%n", a.getNome(), a.getPontos());
    }
  }

  private void menuRecompensas() {
    while (true) {
      System.out.println("\n===== GERENCIAR RECOMPENSAS =====");
      System.out.println("1 - Incluir recompensa");
      System.out.println("2 - Alterar recompensa");
      System.out.println("3 - Excluir recompensa");
      System.out.println("4 - Listar recompensas");
      System.out.println("0 - Voltar");
      int opcao = lerInteiro("Escolha: ");
      if (opcao == 0) return;
      switch (opcao) {
        case 1 -> executarSafely(this::incluirRecompensa);
        case 2 -> executarSafely(this::alterarRecompensa);
        case 3 -> executarSafely(this::excluirRecompensa);
        case 4 -> executarSafely(this::listarRecompensasCrud);
        default -> System.out.println("Opção inválida.");
      }
    }
  }

  private void incluirRecompensa() {
    try {
      String titulo = lerTexto("Título: ");
      int custo = lerInteiro("Custo em pontos: ");
      int estoque = lerInteiro("Estoque: ");
      String erro = "";
      if (titulo.isBlank()) {
        erro = "O título é obrigatório!";
      } else if (custo <= 0 || estoque < 0) {
        erro = "O custo deve ser positivo e o estoque não pode ser negativo!";
      } else if (service.recompensas().buscarPorTitulo(titulo) != null) {
        erro = "Já existe recompensa com este título!";
      }
      if (!erro.isEmpty()) {
        System.out.println("Erro: " + erro);
        return;
      }
      String descricao = lerTextoOuPadrao("Descrição (opcional): ", "");
      Recompensa recompensa = new Recompensa(titulo, descricao, custo, estoque);
      service.recompensas().inserir(recompensa);
      System.out.println("Recompensa cadastrada com sucesso!");
    } catch (SQLException e) {
      System.out.println("Erro: " + e.getMessage());
    }
    catch (IllegalArgumentException e) {
      System.out.println("Erro: " + e.getMessage());
    }
  }

  private void alterarRecompensa() {
    try {
      System.out.println("\nRecompensas cadastradas:");
      listarRecompensasCrud();
      List<Recompensa> recompensas = service.listarRecompensas();
      if (recompensas.isEmpty()) return;
      int escolha = lerInteiro("\nNúmero da recompensa a alterar (0 cancela): ");
      if (escolha == 0) {
        System.out.println("Alteração cancelada.");
        return;
      }
      if (escolha < 0 || escolha > recompensas.size()) {
        System.out.println("Erro: opção inválida!");
        return;
      }
      Recompensa recompensa = recompensas.get(escolha - 1);
      String novoNome = lerTextoOuPadrao("Novo título [" + recompensa.getTitulo() + "]: ", recompensa.getTitulo());
      int novoCusto = lerInteiro("Novo custo: ");
      int novoEstoque = lerInteiro("Novo estoque: ");
      if (novoCusto <= 0 || novoEstoque < 0) {
        System.out.println("Erro: o custo deve ser positivo e o estoque não pode ser negativo!");
        return;
      }
      recompensa.setTitulo(novoNome);
      recompensa.setCusto(novoCusto);
      recompensa.setEstoque(novoEstoque);
      service.recompensas().atualizar(recompensa);
      System.out.println("Recompensa alterada com sucesso!");
    } catch (SQLException e) {
      System.out.println("Erro: " + e.getMessage());
    }
  }

  private void excluirRecompensa() {
    try {
      System.out.println("\nRecompensas cadastradas:");
      List<Recompensa> recompensas = service.listarRecompensas();
      if (recompensas.isEmpty()) {
        System.out.println("Nenhuma recompensa cadastrada.");
        return;
      }
      listarRecompensasCrud();
      int escolha = lerInteiro("\nNúmero da recompensa a excluir (0 cancela): ");
      if (escolha == 0) {
        System.out.println("Exclusão cancelada.");
        return;
      }
      if (escolha < 0 || escolha > recompensas.size()) {
        System.out.println("Erro: opção inválida!");
        return;
      }
      service.excluirRecompensa(recompensas.get(escolha - 1).getId());
      System.out.println("Recompensa excluída com sucesso!");
    } catch (SQLException e) {
      System.out.println("Erro: " + e.getMessage());
    }
  }

  private void listarRecompensasCrud() {
    List<Recompensa> recompensas = service.listarRecompensas();
    if (recompensas.isEmpty()) {
      System.out.println("Nenhuma recompensa cadastrada.");
      return;
    }
    for (int i = 0; i < recompensas.size(); i++) {
      Recompensa r = recompensas.get(i);
      String badge = r.getDestaque().isEmpty() ? "" : " [" + r.getDestaque() + "]";
      String categoria = r.getCategoria().isEmpty() ? "-" : r.getCategoria();
      System.out.printf("%d - %s%s | %s | %d pts | estoque %d%n", i + 1, r.getTitulo(), badge, categoria, r.getCusto(), r.getEstoque());
    }
  }

  private void menuCartas() {
    while (true) {
      System.out.println("\n===== GERENCIAR CARTAS =====");
      System.out.println("1 - Incluir carta");
      System.out.println("2 - Alterar carta");
      System.out.println("3 - Excluir carta");
      System.out.println("4 - Listar cartas");
      System.out.println("0 - Voltar");
      int opcao = lerInteiro("Escolha: ");
      if (opcao == 0) return;
      switch (opcao) {
        case 1 -> executarSafely(this::incluirCarta);
        case 2 -> executarSafely(this::alterarCarta);
        case 3 -> executarSafely(this::excluirCarta);
        case 4 -> executarSafely(this::listarCartasCrud);
        default -> System.out.println("Opção inválida.");
      }
    }
  }

  private void incluirCarta() {
    try {
      String nome = lerTexto("Nome da carta: ");
      String conjunto = lerTexto("Conjunto (Ex.: Recursos, Cidade Verde): ");
      String raridade = lerTextoOuPadrao("Raridade (COMUM, INCOMUM, RARA, EPICA, LENDARIA): ", "COMUM");
      String descricao = lerTextoOuPadrao("Descrição (opcional): ", "");
      String erro = "";
      if (nome.isBlank() || conjunto.isBlank()) {
        erro = "Nome e conjunto são obrigatórios!";
      } else {
        try {
          new Carta(nome, descricao, conjunto, raridade);
        } catch (IllegalArgumentException e) {
          erro = e.getMessage();
        }
      }
      if (!erro.isEmpty()) {
        System.out.println("Erro: " + erro);
        return;
      }
      Carta carta = new Carta(nome, descricao, conjunto, raridade);
      service.cartas().inserir(carta);
      System.out.printf("Carta '%s' cadastrada com sucesso (ID %d)!%n", carta.getNome(), carta.getId());
    } catch (SQLException e) {
      System.out.println("Erro: " + e.getMessage());
    }
  }

  private void alterarCarta() {
    try {
      System.out.println("\nCartas cadastradas:");
      List<Carta> cartas = service.listarCartas();
      if (cartas.isEmpty()) {
        System.out.println("Nenhuma carta cadastrada.");
        return;
      }
      listarCartasCrud();
      int escolha = lerInteiro("\nNúmero da carta a alterar (0 cancela): ");
      if (escolha == 0) {
        System.out.println("Alteração cancelada.");
        return;
      }
      if (escolha < 0 || escolha > cartas.size()) {
        System.out.println("Erro: opção inválida!");
        return;
      }
      Carta carta = cartas.get(escolha - 1);
      String novoNome = lerTextoOuPadrao("Novo nome [" + carta.getNome() + "]: ", carta.getNome());
      String novoConjunto = lerTextoOuPadrao("Novo conjunto [" + carta.getConjunto() + "]: ", carta.getConjunto());
      String novaRaridade = lerTextoOuPadrao("Nova raridade [" + carta.getRaridade() + "]: ", carta.getRaridade());
      String novaDescricao = lerTextoOuPadrao("Nova descrição [" + carta.getDescricao() + "]: ", carta.getDescricao());
      try {
        carta.setNome(novoNome);
        carta.setConjunto(novoConjunto);
        carta.setRaridade(novaRaridade);
        carta.setDescricao(novaDescricao);
      } catch (IllegalArgumentException e) {
        System.out.println("Erro: " + e.getMessage());
        return;
      }
      service.cartas().atualizar(carta);
      System.out.println("Carta alterada com sucesso!");
    } catch (SQLException e) {
      System.out.println("Erro: " + e.getMessage());
    }
  }

  private void excluirCarta() {
    try {
      System.out.println("\nCartas cadastradas:");
      List<Carta> cartas = service.listarCartas();
      if (cartas.isEmpty()) {
        System.out.println("Nenhuma carta cadastrada.");
        return;
      }
      listarCartasCrud();
      int escolha = lerInteiro("\nNúmero da carta a excluir (0 cancela): ");
      if (escolha == 0) {
        System.out.println("Exclusão cancelada.");
        return;
      }
      if (escolha < 0 || escolha > cartas.size()) {
        System.out.println("Erro: opção inválida!");
        return;
      }
      service.excluirCarta(cartas.get(escolha - 1).getId());
      System.out.println("Carta excluída com sucesso!");
    } catch (SQLException e) {
      System.out.println("Erro: " + e.getMessage());
    }
  }

  private void listarCartasCrud() {
    List<Carta> cartas = service.listarCartas();
    if (cartas.isEmpty()) {
      System.out.println("Nenhuma carta cadastrada.");
      return;
    }
    for (int i = 0; i < cartas.size(); i++) {
      Carta c = cartas.get(i);
      System.out.printf(
        "%d - %s | %s | %s%n",
        i + 1, c.getNome(), c.getConjunto(), c.getRaridade());
    }
  }

  /* ─────────────── Utilitários ─────────────── */

  private void executarSafely(Runnable acao) {
    try {
      acao.run();
    } catch (IllegalArgumentException | IllegalStateException e) {
      System.out.println("Erro: " + e.getMessage());
    } catch (RuntimeException e) {
      System.out.println("Erro inesperado: " + e.getMessage());
    }
  }

  private void anunciarSelosNovos(List<Selo> selos) {
    if (!selos.isEmpty()) {
      System.out.println("Selos conquistados agora: " + nomes(selos));
    }
  }

  private String nomes(List<Selo> selos) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < selos.size(); i++) {
      if (i > 0) sb.append(", ");
      sb.append(selos.get(i).getNome());
    }
    return sb.toString();
  }

  private List<Usuario> listarUsuarios() {
    try {
      return service.usuarios().listar();
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao listar usuários: " + e.getMessage(), e);
    }
  }

  private List<Historico> listarHistorico() {
    try {
      return service.historico().listarPorUsuario(usuarioLogado.getId());
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao consultar histórico: " + e.getMessage(), e);
    }
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
    return lerTexto(rotulo, 100);
  }

  private String lerTexto(String rotulo, int maxLength) {
    while (true) {
      System.out.print(rotulo);
      String valor = scanner.nextLine().trim();
      if (valor.isEmpty()) {
        System.out.println("Valor obrigatório.");
        continue;
      }
      if (valor.length() > maxLength) {
        System.out.println("Valor muito longo: máximo de " + maxLength + " caracteres.");
        continue;
      }
      return valor;
    }
  }

  private long lerId(String rotulo) {
    while (true) {
      long id = lerInteiro(rotulo);
      if (id > 0) return id;
      System.out.println("O ID deve ser um número positivo.");
    }
  }

  private String lerTextoOuPadrao(String rotulo, String padrao) {
    System.out.print(rotulo);
    String valor = scanner.nextLine().trim();
    return valor.isEmpty() ? padrao : valor;
  }
}
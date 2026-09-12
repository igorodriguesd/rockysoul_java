package br.com.rockysoulup.service;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.exception.RegistroDuplicadoException;
import br.com.rockysoulup.exception.SaldoInsuficienteException;
import br.com.rockysoulup.model.*;
import br.com.rockysoulup.repository.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public final class RockySoulService {

  private final GamificacaoService gamificacao = new GamificacaoService();
  private final UsuarioRepository usuarioRepository = new UsuarioRepository();
  private final HistoricoRepository historicoRepository = new HistoricoRepository();
  private final SeloRepository seloRepository = new SeloRepository();
  private final AcaoRepository acaoRepository = new AcaoRepository();
  private final RecompensaRepository recompensaRepository = new RecompensaRepository();
  private final CartaRepository cartaRepository = new CartaRepository();
  private final UsuarioCartaRepository usuarioCartaRepository = new UsuarioCartaRepository();
  private final UsuarioFragmentoRepository usuarioFragmentoRepository = new UsuarioFragmentoRepository();

  private static final int CHANCE_DROP_POR_RARIDADE_COMUM = 75;
  private static final int CHANCE_DROP_POR_RARIDADE_INCOMUM = 55;
  private static final int CHANCE_DROP_POR_RARIDADE_RARA = 35;
  private static final int CHANCE_DROP_POR_RARIDADE_EPICA = 20;
  private static final int CHANCE_DROP_POR_RARIDADE_LENDARIA = 10;

  private static final int FRAGMENTOS_POR_DUPLICADA_COMUM = 3;
  private static final int FRAGMENTOS_POR_DUPLICADA_INCOMUM = 5;
  private static final int FRAGMENTOS_POR_DUPLICADA_RARA = 10;
  private static final int FRAGMENTOS_POR_DUPLICADA_EPICA = 20;
  private static final int FRAGMENTOS_POR_DUPLICADA_LENDARIA = 40;

  private static final int CUSTO_FABRICACAO_COMUM = 10;
  private static final int CUSTO_FABRICACAO_INCOMUM = 16;
  private static final int CUSTO_FABRICACAO_RARA = 32;
  private static final int CUSTO_FABRICACAO_EPICA = 70;
  private static final int CUSTO_FABRICACAO_LENDARIA = 140;

  // ───── Métodos auxiliares de raridade ─────

  public static int chanceDrop(String raridade) {
    return switch (raridade) {
      case "COMUM" -> CHANCE_DROP_POR_RARIDADE_COMUM;
      case "INCOMUM" -> CHANCE_DROP_POR_RARIDADE_INCOMUM;
      case "RARA" -> CHANCE_DROP_POR_RARIDADE_RARA;
      case "EPICA" -> CHANCE_DROP_POR_RARIDADE_EPICA;
      case "LENDARIA" -> CHANCE_DROP_POR_RARIDADE_LENDARIA;
      default -> 0;
    };
  }

  public static int fragmentosPorDuplicada(String raridade) {
    return switch (raridade) {
      case "COMUM" -> FRAGMENTOS_POR_DUPLICADA_COMUM;
      case "INCOMUM" -> FRAGMENTOS_POR_DUPLICADA_INCOMUM;
      case "RARA" -> FRAGMENTOS_POR_DUPLICADA_RARA;
      case "EPICA" -> FRAGMENTOS_POR_DUPLICADA_EPICA;
      case "LENDARIA" -> FRAGMENTOS_POR_DUPLICADA_LENDARIA;
      default -> 0;
    };
  }

  public static int custoFabricacao(String raridade) {
    return switch (raridade) {
      case "COMUM" -> CUSTO_FABRICACAO_COMUM;
      case "INCOMUM" -> CUSTO_FABRICACAO_INCOMUM;
      case "RARA" -> CUSTO_FABRICACAO_RARA;
      case "EPICA" -> CUSTO_FABRICACAO_EPICA;
      case "LENDARIA" -> CUSTO_FABRICACAO_LENDARIA;
      default -> 0;
    };
  }

  // ───── Drop de carta pós-ação (igual ao React) ─────

  public Optional<ResultadoSorteio> tentarDropCarta(Usuario usuario, Connection connection) throws SQLException {
    List<Carta> catalogo = cartaRepository.listar();
    if (catalogo.isEmpty()) return Optional.empty();

    String raridadeSorteada = Carta.sortearRaridadeAleatoria();
    int chance = chanceDrop(raridadeSorteada);

    if ((int) (Math.random() * 100) >= chance) return Optional.empty();

    List<Carta> cartasDaRaridade = catalogo.stream()
        .filter(c -> c.getRaridade().equals(raridadeSorteada))
        .toList();
    if (cartasDaRaridade.isEmpty()) return Optional.empty();

    Carta sorteada = cartasDaRaridade.get((int) (Math.random() * cartasDaRaridade.size()));
    boolean jaTem = usuarioCartaRepository.jaPossui(connection, usuario.getId(), sorteada.getId());

    if (!jaTem) {
      usuarioCartaRepository.inserir(connection, new UsuarioCarta(usuario.getId(), sorteada.getId(), 1));
      return Optional.of(new ResultadoSorteio(true, sorteada, true, 0));
    }
    int fragmentos = fragmentosPorDuplicada(sorteada.getRaridade());
    usuarioFragmentoRepository.adicionar(connection, usuario.getId(), sorteada.getRaridade(), fragmentos);
    return Optional.of(new ResultadoSorteio(true, sorteada, false, fragmentos));
  }

  public ResultadoSorteio sortearCartaGratis(Usuario usuario) {
    try (Connection con = ConnectionFactory.abrir()) {
      con.setAutoCommit(false);
      try {
        ResultadoSorteio sorteio = tentarDropCarta(usuario, con)
            .orElse(ResultadoSorteio.semCarta());
        con.commit();
        return sorteio;
      } catch (SQLException | RuntimeException e) {
        con.rollback();
        throw e;
      }
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao sortear a carta: " + e.getMessage(), e);
    }
  }

  public record ResultadoSorteio(boolean caiu, Carta carta, boolean nova, int fragmentosGanhos) {

    public static ResultadoSorteio semCarta() {
      return new ResultadoSorteio(false, null, false, 0);
    }
  }

  // ───── Fabricar carta com fragmentos ─────

  public Carta fabricarCarta(Usuario usuario, long idCarta) throws SaldoInsuficienteException {
    try {
      Carta carta = cartaRepository.buscarPorId(idCarta);
      if (carta == null) throw new IllegalStateException("Carta não encontrada!");

      int custo = custoFabricacao(carta.getRaridade());
      int temFragmentos;
      try (Connection con = ConnectionFactory.abrir()) {
        if (usuarioCartaRepository.jaPossui(con, usuario.getId(), carta.getId())) {
          throw new IllegalStateException("Você já possui esta carta!");
        }
        temFragmentos = usuarioFragmentoRepository.buscar(con, usuario.getId(), carta.getRaridade());
      }
      if (temFragmentos < custo) {
        throw new SaldoInsuficienteException(
            "Fragmentos insuficientes! Precisa de " + custo + " " + carta.getRaridade() + ".");
      }
      emTransacao(connection -> {
        usuarioFragmentoRepository.gastar(connection, usuario.getId(), carta.getRaridade(), custo);
        usuarioCartaRepository.inserir(connection, new UsuarioCarta(usuario.getId(), carta.getId(), 1));
      });
      return carta;
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao fabricar carta: " + e.getMessage(), e);
    }
  }

  // ───── Consultas de coleção ─────

  public List<Carta> listarCartas() {
    try {
      return cartaRepository.listar();
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao listar cartas: " + e.getMessage(), e);
    }
  }

  public List<Carta> listarCartasDoUsuario(Usuario usuario) {
    try {
      List<Carta> cartas = new ArrayList<>();
      for (UsuarioCarta uc : usuarioCartaRepository.listarPorUsuario(usuario.getId())) {
        Carta carta = cartaRepository.buscarPorId(uc.getCartaId());
        if (carta != null) cartas.add(carta);
      }
      return cartas;
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao listar cartas do usuário: " + e.getMessage(), e);
    }
  }

  public int quantidadeCartaDoUsuario(Usuario usuario, long cartaId) {
    try {
      List<UsuarioCarta> lista = usuarioCartaRepository.listarPorUsuario(usuario.getId());
      for (UsuarioCarta uc : lista) {
        if (uc.getCartaId() == cartaId) return uc.getQuantidade();
      }
      return 0;
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao consultar a coleção: " + e.getMessage(), e);
    }
  }

  public Map<String, Integer> fragmentosDoUsuario(Usuario usuario) {
    try (Connection con = ConnectionFactory.abrir()) {
      return usuarioFragmentoRepository.listarPorUsuario(con, usuario.getId());
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao consultar fragmentos: " + e.getMessage(), e);
    }
  }

  // ───── Fluxo de ação + drop ─────

  public ResultadoAcao registrarAcao(Usuario usuario, String descricao, int pontos) {
    try {
      int pontosAntes = usuario.getPontos();
      gamificacao.registrarAcao(usuario, pontos);
      List<Selo> novosSelos = selosNovos(usuario, pontosAntes);
      Optional<ResultadoSorteio> drop = Optional.empty();
      emTransacao(connection -> {
        historicoRepository.inserir(connection, new Historico(usuario.getId(), descricao, pontos));
        usuarioRepository.atualizar(connection, usuario);
      });
      try (Connection con = ConnectionFactory.abrir()) {
        con.setAutoCommit(false);
        try {
          drop = tentarDropCarta(usuario, con);
          con.commit();
        } catch (SQLException | RuntimeException ex) {
          con.rollback();
          throw ex;
        }
      }
      return new ResultadoAcao(novosSelos, drop);
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao registrar a ação: " + e.getMessage(), e);
    }
  }

  public Usuario cadastrarUsuario(String nome, String email) throws RegistroDuplicadoException {
    try {
      if (usuarioRepository.buscarPorEmail(email) != null) {
        throw new RegistroDuplicadoException("E-mail já cadastrado. Use outro e-mail.");
      }

      Usuario novo = new Usuario(nome, email);
      emTransacao(connection -> usuarioRepository.inserir(connection, novo));
      return novo;
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao acessar o banco: " + e.getMessage(), e);
    }
  }

  public void garantirCatalogo() {
    try {
      if (acaoRepository.listar().isEmpty()) {
        emTransacao(connection -> {
          acaoRepository.inserir(connection, new Acao("Reciclagem", 30));
          acaoRepository.inserir(connection, new Acao("Transporte Público", 50));
          acaoRepository.inserir(connection, new Acao("Economia de Energia", 20));
          acaoRepository.inserir(connection, new Acao("Economia de Água", 15));
          acaoRepository.inserir(connection, new Acao("Bicicleta", 25));
          acaoRepository.inserir(connection, new Acao("Plantio de Árvore", 100));
          acaoRepository.inserir(connection, new Acao("Banho Rápido", 20));
        });
      }
      if (recompensaRepository.listar().isEmpty()) {
        emTransacao(connection -> {
          recompensaRepository.inserir(connection,
              new Recompensa("Desconto Energia", "10% de desconto na conta de energia", 200, 50, "Energia", "Novo"));
          recompensaRepository.inserir(connection,
              new Recompensa("Passe de Transporte", "Um passe livre de transporte público", 350, 15, "Transporte", ""));
          recompensaRepository.inserir(connection,
              new Recompensa("Muda de Árvore", "Receba uma muda para plantar", 500, 20, "Natureza", "Top 1"));
          recompensaRepository.inserir(connection,
              new Recompensa("Cupom Reciclagem", "Cupom de R$15 em lojas parceiras", 150, 30, "Cupons", ""));
          recompensaRepository.inserir(connection, new Recompensa("Kit Sustentável",
              "Kit com canudo reutilizável e sacola ecológica", 250, 20, "Natureza", ""));
          recompensaRepository.inserir(connection,
              new Recompensa("Desconto Água", "5% de desconto na conta de água", 180, 30, "Energia", ""));
          recompensaRepository.inserir(connection,
              new Recompensa("Cupom Bicicleta", "Cupom de R$20 em bicicletarias", 400, 10, "Transporte", ""));
          recompensaRepository.inserir(connection,
              new Recompensa("Adoção de Árvore", "Adote uma árvore real por 3 meses", 800, 2, "Natureza", "Top 1"));
        });
      }
      if (seloRepository.listar().isEmpty()) {
        emTransacao(connection -> {
          seloRepository.inserir(connection, new Selo("Semente", "Primeiros passos sustentáveis", 100));
          seloRepository.inserir(connection, new Selo("Broto", "Crescendo em sustentabilidade", 300));
          seloRepository.inserir(connection, new Selo("Árvore", "Impacto real no planeta", 600));
          seloRepository.inserir(connection, new Selo("Expert", "Lenda da sustentabilidade", 1000));
        });
      }
      if (cartaRepository.listar().isEmpty()) {
        emTransacao(connection -> {
          for (Carta carta : catalogoPadrao()) {
            cartaRepository.inserir(connection, carta);
          }
        });
      }
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao preparar o catálogo: " + e.getMessage(), e);
    }
  }

  public List<Carta> catalogoPadrao() {
    return List.of(
        new Carta("Reciclagem", "Separar corretamente seus resíduos.", "Recursos", "COMUM"),
        new Carta("Reutilização", "Dar nova vida a objetos e compartilhar dicas.", "Recursos", "INCOMUM"),
        new Carta("Sacola Reutilizável", "Usar sacolas ecológicas no lugar das descartáveis.", "Recursos", "RARA"),
        new Carta("Redução de Desperdício", "Consumo consciente com menos desperdício.", "Recursos", "EPICA"),

        new Carta("Economia de Água", "Reduzir o consumo diário de água.", "Guardiões da Água", "COMUM"),
        new Carta("Banho Rápido", "Tomar banhos curtos e conscientes.", "Guardiões da Água", "INCOMUM"),
        new Carta("Garrafa Reutilizável", "Adotar garrafa própria no lugar de descartáveis.", "Guardiões da Água",
            "RARA"),
        new Carta("Captação de Chuva", "Aproveitar a água da chuva.", "Guardiões da Água", "EPICA"),

        new Carta("Bicicleta", "Pedalar no lugar de usar o carro.", "Cidade Verde", "COMUM"),
        new Carta("Transporte Público", "Priorizar ônibus e metrô.", "Cidade Verde", "INCOMUM"),
        new Carta("Mobilidade Elétrica", "Optar por veículos e patinetes elétricos.", "Cidade Verde", "RARA"),
        new Carta("Ciclovia", "Apoiar e usar infraestrutura cicloviária.", "Cidade Verde", "EPICA"),

        new Carta("Economia de Energia", "Reduzir o consumo de eletricidade em casa.", "Energia Limpa", "COMUM"),
        new Carta("Iluminação Eficiente", "Trocar lâmpadas por modelos eficientes.", "Energia Limpa", "INCOMUM"),
        new Carta("Energia Solar", "Gerar energia a partir do sol.", "Energia Limpa", "RARA"),
        new Carta("Energia Eólica", "Aproveitar a força dos ventos.", "Energia Limpa", "EPICA"),

        new Carta("Plantio", "Plantar árvores e espécies nativas.", "Cultivo", "COMUM"),
        new Carta("Compostagem", "Transformar resíduos orgânicos em adubo.", "Cultivo", "INCOMUM"),
        new Carta("Horta Doméstica", "Cultivar alimentos em casa.", "Cultivo", "RARA"),
        new Carta("Agrofloresta", "Sistema integrado de cultivo com a floresta.", "Cultivo", "LENDARIA"));
  }

  // ───── Catálogo padrão (já existente) ─────

  /** Selos cuja pontuação mínima foi atingida por esta ação (comparando antes/depois). */
  private List<Selo> selosNovos(Usuario usuario, int pontosAntes) throws SQLException {
    List<Selo> novos = new ArrayList<>();
    for (Selo selo : seloRepository.listar()) {
      boolean antes = pontosAntes >= selo.getPontosMin();
      boolean agora = usuario.getPontos() >= selo.getPontosMin();
      if (!antes && agora) novos.add(selo);
    }
    return novos;
  }

  public List<Selo> listarSelosConcedidos(Usuario usuario) {
    try {
      return seloRepository.listar().stream()
          .filter(selo -> usuario.getPontos() >= selo.getPontosMin())
          .toList();
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao consultar selos: " + e.getMessage(), e);
    }
  }

  public List<Selo> listarSelos() {
    try {
      return seloRepository.listar();
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao listar selos: " + e.getMessage(), e);
    }
  }

  public List<Acao> listarAcoes() {
    try {
      return acaoRepository.listar();
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao listar ações: " + e.getMessage(), e);
    }
  }

  public List<Recompensa> listarRecompensas() {
    try {
      return recompensaRepository.listar();
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao listar recompensas: " + e.getMessage(), e);
    }
  }

  public Recompensa resgatarRecompensa(Usuario usuario, long idRecompensa) throws SaldoInsuficienteException {
    try {
      Recompensa recompensa = recompensaRepository.buscarPorId(idRecompensa);
      if (recompensa == null) {
        throw new IllegalStateException("Erro: recompensa não encontrada!");
      }
      if (recompensa.getEstoque() <= 0) {
        throw new IllegalStateException("Erro: recompensa esgotada!");
      }
      if (usuario.getPontos() < recompensa.getCusto()) {
        throw new SaldoInsuficienteException(
            "Pontos insuficientes! Você precisa de " +
                recompensa.getCusto() +
                ", mas tem apenas " +
                usuario.getPontos() +
                ".");
      }
      emTransacao(connection -> {
        recompensa.setEstoque(recompensa.getEstoque() - 1);
        recompensaRepository.atualizar(connection, recompensa);
        usuario.setPontos(usuario.getPontos() - recompensa.getCusto());
        usuario.setResgatados(usuario.getResgatados() + recompensa.getCusto());
        usuarioRepository.atualizar(connection, usuario);
      });
      return recompensa;
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao resgatar a recompensa: " + e.getMessage(), e);
    }
  }

  public HistoricoRepository historico() {
    return historicoRepository;
  }

  public UsuarioRepository usuarios() {
    return usuarioRepository;
  }

  public AcaoRepository acoes() {
    return acaoRepository;
  }

  public RecompensaRepository recompensas() {
    return recompensaRepository;
  }

  public CartaRepository cartas() {
    return cartaRepository;
  }

  public void excluirCarta(long id) throws SQLException {
    emTransacao(connection -> {
      usuarioCartaRepository.excluirPorCarta(connection, id);
      cartaRepository.excluir(connection, id);
    });
  }

  public void excluirUsuario(long id) throws SQLException {
    emTransacao(connection -> {
      historicoRepository.excluirPorUsuario(connection, id);
      usuarioCartaRepository.excluirPorUsuario(connection, id);
      usuarioFragmentoRepository.excluirPorUsuario(connection, id);
      usuarioRepository.excluir(connection, id);
    });
  }

  public void excluirSelo(long id) throws SQLException {
    emTransacao(connection -> seloRepository.excluir(connection, id));
  }

  public void excluirAcao(long id) throws SQLException {
    emTransacao(connection -> acaoRepository.excluir(connection, id));
  }

  public void excluirRecompensa(long id) throws SQLException {
    emTransacao(connection -> recompensaRepository.excluir(connection, id));
  }

  private interface Transacao {

    void executar(Connection connection) throws SQLException;
  }

  private void emTransacao(Transacao transacao) throws SQLException {
    try (Connection connection = ConnectionFactory.abrir()) {
      connection.setAutoCommit(false);
      try {
        transacao.executar(connection);
        connection.commit();
      } catch (SQLException | RuntimeException e) {
        connection.rollback();
        throw e;
      }
    }
  }

  public record ResultadoAcao(List<Selo> selosConquistados, Optional<ResultadoSorteio> cartaDrop) {}
}
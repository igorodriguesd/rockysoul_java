package br.com.rockysoulup.service;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.model.*;
import br.com.rockysoulup.repository.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Orquestra as operações do sistema sobre o Oracle, com transações. */
public final class RockySoulService {

  private final GamificacaoService gamificacao = new GamificacaoService();
  private final UsuarioRepository usuarioRepository = new UsuarioRepository();
  private final HistoricoRepository historicoRepository = new HistoricoRepository();
  private final SeloRepository seloRepository = new SeloRepository();
  private final UsuarioSeloRepository usuarioSeloRepository = new UsuarioSeloRepository();
  private final AcaoRepository acaoRepository = new AcaoRepository();
  private final RecompensaRepository recompensaRepository = new RecompensaRepository();
  private final CartaRepository cartaRepository = new CartaRepository();
  private final UsuarioCartaRepository usuarioCartaRepository = new UsuarioCartaRepository();

  /**
   * Cadastra um novo usuário; rejeita e-mails já cadastrados (regra de e-mail
   * único).
   */
  public Usuario cadastrarUsuario(String nome, String email) {
    try {
      if (usuarioRepository.buscarPorEmail(email) != null) {
        throw new IllegalStateException("E-mail já cadastrado. Use outro e-mail.");
      }

      Usuario novo = new Usuario(nome, email);
      emTransacao(connection -> usuarioRepository.inserir(connection, novo));
      return novo;
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao acessar o banco: " + e.getMessage(), e);
    }
  }

  // Garante o catálogo padrão (ações, recompensas e selos) quando o banco está
  // vazio
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

  /** Registra uma ação, credita pontos e concede selos automaticamente. */
  public List<Selo> registrarAcao(Usuario usuario, String descricao, int pontos) {
    try {
      gamificacao.registrarAcao(usuario, pontos);
      List<Selo> concedidos = new ArrayList<>();
      emTransacao(connection -> {
        historicoRepository.inserir(
            connection,
            new Historico(usuario.getId(), descricao, pontos));
        usuarioRepository.atualizar(connection, usuario);
        concedidos.addAll(concederSelos(connection, usuario));
      });
      return concedidos;
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao registrar a ação: " + e.getMessage(), e);
    }
  }

  /** Concede os selos cujo mínimo já foi atingido e que ainda não foram dados. */
  private List<Selo> concederSelos(Connection connection, Usuario usuario)
      throws SQLException {
    List<Selo> concedidos = new ArrayList<>();
    for (Selo selo : seloRepository.listar()) {
      boolean jaTem = usuarioSeloRepository.jaConquistado(connection, usuario.getId(), selo.getId());
      if (!jaTem && gamificacao.seloConquistado(usuario, selo)) {
        usuarioSeloRepository.inserir(connection, new UsuarioSelo(usuario.getId(), selo.getId()));
        concedidos.add(selo);
      }
    }
    return concedidos;
  }

  /** Recalcula e concede selos de um usuário sem mudar pontuação. */
  public List<Selo> listarSelosConcedidos(Usuario usuario) {
    try {
      List<Selo> concedidos = new ArrayList<>();
      for (Selo selo : seloRepository.listar()) {
        if (usuarioSeloRepository.jaConquistado(usuario.getId(), selo.getId())) {
          concedidos.add(selo);
        }
      }
      return concedidos;
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
      for (UsuarioCarta usuarioCarta : usuarioCartaRepository.listarPorUsuario(usuario.getId())) {
        Carta carta = cartaRepository.buscarPorId(usuarioCarta.getCartaId());
        if (carta != null) {
          cartas.add(carta);
        }
      }
      return cartas;
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao listar cartas do usuário: " + e.getMessage(), e);
    }
  }

  public Carta adicionarCartaAoUsuario(Usuario usuario, long idCarta) {
    try {
      Carta carta = cartaRepository.buscarPorId(idCarta);
      if (carta == null) {
        throw new IllegalStateException("Erro: carta não encontrada!");
      }
      emTransacao(connection -> {
        if (usuarioCartaRepository.jaPossui(connection, usuario.getId(), carta.getId())) {
          usuarioCartaRepository.atualizarQuantidade(connection, usuario.getId(), carta.getId(), 1);
        } else {
          usuarioCartaRepository.inserir(connection, new UsuarioCarta(usuario.getId(), carta.getId(), 1));
        }
      });
      return carta;
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao adicionar carta: " + e.getMessage(), e);
    }
  }

  public Carta sortearCartaPorRaridade(List<Carta> catalogo) {
    if (catalogo == null || catalogo.isEmpty()) {
      throw new IllegalStateException("Erro: catálogo de cartas vazio!");
    }

    String raridadeSorteada = Carta.sortearRaridadeAleatoria();
    List<Carta> cartasDaRaridade = catalogo.stream()
        .filter(c -> c.getRaridade().equals(raridadeSorteada))
        .toList();

    if (cartasDaRaridade.isEmpty()) {
      return catalogo.get(0);
    }

    int indice = (int) (Math.random() * cartasDaRaridade.size());
    return cartasDaRaridade.get(indice);
  }

  /**
   * Resgata uma recompensa: valida estoque/saldo, desconta pontos e baixa
   * estoque.
   */
  public Recompensa resgatarRecompensa(Usuario usuario, long idRecompensa) {
    try {
      Recompensa recompensa = recompensaRepository.buscarPorId(idRecompensa);
      if (recompensa == null) {
        throw new IllegalStateException("Erro: recompensa não encontrada!");
      }
      if (recompensa.getEstoque() <= 0) {
        throw new IllegalStateException("Erro: recompensa esgotada!");
      }
      if (usuario.getPontos() < recompensa.getCusto()) {
        throw new IllegalStateException(
            "Erro: pontos insuficientes! Você precisa de " +
                recompensa.getCusto() +
                ", mas tem apenas " +
                usuario.getPontos() +
                ".");
      }
      emTransacao(connection -> {
        recompensa.setEstoque(recompensa.getEstoque() - 1);
        recompensaRepository.atualizar(recompensa);
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

  /** Exclui um usuário com todos os dependentes, em uma única transação. */
  public void excluirUsuario(long id) throws SQLException {
    emTransacao(connection -> {
      historicoRepository.excluirPorUsuario(connection, id);
      usuarioSeloRepository.excluirPorUsuario(connection, id);
      usuarioRepository.excluir(connection, id);
    });
  }

  /** Exclui um selo e seus vínculos com usuários, em uma única transação. */
  public void excluirSelo(long id) throws SQLException {
    emTransacao(connection -> {
      usuarioSeloRepository.excluirPorSelo(connection, id);
      seloRepository.excluir(connection, id);
    });
  }

  /** Exclui uma ação do catálogo. */
  public void excluirAcao(long id) throws SQLException {
    emTransacao(connection -> acaoRepository.excluir(connection, id));
  }

  /** Exclui uma recompensa do catálogo. */
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
}
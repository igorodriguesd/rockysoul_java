package br.com.rockysoulup.service;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.dao.*;
import br.com.rockysoulup.model.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Orquestra as operações do sistema sobre o Oracle, com transações. */
public final class RockySoulService {

  private final GamificacaoService gamificacao = new GamificacaoService();
  private final UsuarioDao usuarioDao = new UsuarioDao();
  private final HistoricoDao historicoDao = new HistoricoDao();
  private final SeloDao seloDao = new SeloDao();
  private final UsuarioSeloDao usuarioSeloDao = new UsuarioSeloDao();
  private final AcaoDao acaoDao = new AcaoDao();
  private final RecompensaDao recompensaDao = new RecompensaDao();

  /** Cadastra um novo usuário; rejeita e-mails já cadastrados (regra de e-mail único). */
  public Usuario cadastrarUsuario(String nome, String email) {
    try {
      if (usuarioDao.buscarPorEmail(email) != null) {
        throw new IllegalStateException("E-mail já cadastrado. Use outro e-mail.");
      }

      Usuario novo = new Usuario(nome, email);
      emTransacao(connection -> usuarioDao.inserir(connection, novo));
      return novo;
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao acessar o banco: " + e.getMessage(), e);
    }
  }

  /**
   * Garante o catálogo padrão quando a base está vazia: ações sustentáveis,
   * recompensas resgatáveis e selos de conquista (espelha o site / Python).
   */
  public void garantirCatalogo() {
    try {
      if (acaoDao.listar().isEmpty()) {
        emTransacao(connection -> {
          acaoDao.inserir(connection, new Acao("Reciclagem", 30));
          acaoDao.inserir(connection, new Acao("Transporte Público", 50));
          acaoDao.inserir(connection, new Acao("Economia de Energia", 20));
          acaoDao.inserir(connection, new Acao("Economia de Água", 15));
          acaoDao.inserir(connection, new Acao("Bicicleta", 25));
          acaoDao.inserir(connection, new Acao("Plantio de Árvore", 100));
          acaoDao.inserir(connection, new Acao("Banho Rápido", 20));
        });
      }
      if (recompensaDao.listar().isEmpty()) {
        emTransacao(connection -> {
          recompensaDao.inserir(connection, new Recompensa("Desconto Energia", "10% de desconto na conta de energia", 200, 50, "Energia", "Novo"));
          recompensaDao.inserir(connection, new Recompensa("Passe de Transporte", "Um passe livre de transporte público", 350, 15, "Transporte", ""));
          recompensaDao.inserir(connection, new Recompensa("Muda de Árvore", "Receba uma muda para plantar", 500, 20, "Natureza", "Top 1"));
          recompensaDao.inserir(connection, new Recompensa("Cupom Reciclagem", "Cupom de R$15 em lojas parceiras", 150, 30, "Cupons", ""));
          recompensaDao.inserir(connection, new Recompensa("Kit Sustentável", "Kit com canudo reutilizável e sacola ecológica", 250, 20, "Natureza", ""));
          recompensaDao.inserir(connection, new Recompensa("Desconto Água", "5% de desconto na conta de água", 180, 30, "Energia", ""));
          recompensaDao.inserir(connection, new Recompensa("Cupom Bicicleta", "Cupom de R$20 em bicicletarias", 400, 10, "Transporte", ""));
          recompensaDao.inserir(connection, new Recompensa("Adoção de Árvore", "Adote uma árvore real por 3 meses", 800, 2, "Natureza", "Top 1"));
        });
      }
      if (seloDao.listar().isEmpty()) {
        emTransacao(connection -> {
          seloDao.inserir(connection, new Selo("Semente", "Primeiros passos sustentáveis", 100));
          seloDao.inserir(connection, new Selo("Broto", "Crescendo em sustentabilidade", 300));
          seloDao.inserir(connection, new Selo("Árvore", "Impacto real no planeta", 600));
          seloDao.inserir(connection, new Selo("Expert", "Lenda da sustentabilidade", 1000));
        });
      }
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao preparar o catálogo: " + e.getMessage(), e);
    }
  }

  /** Registra uma ação, credita pontos e concede selos automaticamente. */
  public List<Selo> registrarAcao(Usuario usuario, String descricao, int pontos) {
    try {
      gamificacao.registrarAcao(usuario, pontos);
      List<Selo> concedidos = new ArrayList<>();
      emTransacao(connection -> {
        historicoDao.inserir(
          connection,
          new Historico(usuario.getId(), descricao, pontos)
        );
        usuarioDao.atualizar(connection, usuario);
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
    for (Selo selo : seloDao.listar()) {
      boolean jaTem = usuarioSeloDao.jaConquistado(connection, usuario.getId(), selo.getId());
      if (!jaTem && gamificacao.seloConquistado(usuario, selo)) {
        usuarioSeloDao.inserir(connection, new UsuarioSelo(usuario.getId(), selo.getId()));
        concedidos.add(selo);
      }
    }
    return concedidos;
  }

  /** Recalcula e concede selos de um usuário sem mudar pontuação. */
  public List<Selo> listarSelosConcedidos(Usuario usuario) {
    try {
      List<Selo> concedidos = new ArrayList<>();
      for (Selo selo : seloDao.listar()) {
        if (usuarioSeloDao.jaConquistado(usuario.getId(), selo.getId())) {
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
      return seloDao.listar();
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao listar selos: " + e.getMessage(), e);
    }
  }

  public List<Acao> listarAcoes() {
    try {
      return acaoDao.listar();
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao listar ações: " + e.getMessage(), e);
    }
  }

  public List<Recompensa> listarRecompensas() {
    try {
      return recompensaDao.listar();
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao listar recompensas: " + e.getMessage(), e);
    }
  }

  /** Resgata uma recompensa: valida estoque/saldo, desconta pontos e baixa estoque. */
  public Recompensa resgatarRecompensa(Usuario usuario, long idRecompensa) {
    try {
      Recompensa recompensa = recompensaDao.buscarPorId(idRecompensa);
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
          "."
        );
      }
      emTransacao(connection -> {
        recompensa.setEstoque(recompensa.getEstoque() - 1);
        recompensaDao.atualizar(recompensa);
        usuario.setPontos(usuario.getPontos() - recompensa.getCusto());
        usuario.setResgatados(usuario.getResgatados() + recompensa.getCusto());
        usuarioDao.atualizar(connection, usuario);
      });
      return recompensa;
    } catch (SQLException e) {
      throw new IllegalStateException("Falha ao resgatar a recompensa: " + e.getMessage(), e);
    }
  }

  public HistoricoDao historico() {
    return historicoDao;
  }

  public UsuarioDao usuarios() {
    return usuarioDao;
  }

  public AcaoDao acoes() {
    return acaoDao;
  }

  public RecompensaDao recompensas() {
    return recompensaDao;
  }

  /** Exclui um usuário com todos os dependentes, em uma única transação. */
  public void excluirUsuario(long id) throws SQLException {
    emTransacao(connection -> {
      historicoDao.excluirPorUsuario(connection, id);
      usuarioSeloDao.excluirPorUsuario(connection, id);
      usuarioDao.excluir(connection, id);
    });
  }

  /** Exclui um selo e seus vínculos com usuários, em uma única transação. */
  public void excluirSelo(long id) throws SQLException {
    emTransacao(connection -> {
      usuarioSeloDao.excluirPorSelo(connection, id);
      seloDao.excluir(connection, id);
    });
  }

  /** Exclui uma ação do catálogo. */
  public void excluirAcao(long id) throws SQLException {
    emTransacao(connection -> acaoDao.excluir(connection, id));
  }

  /** Exclui uma recompensa do catálogo. */
  public void excluirRecompensa(long id) throws SQLException {
    emTransacao(connection -> recompensaDao.excluir(connection, id));
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
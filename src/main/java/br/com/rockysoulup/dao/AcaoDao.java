package br.com.rockysoulup.dao;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.model.Acao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class AcaoDao {

  public void inserir(Acao acao) throws SQLException {
    try (Connection c = ConnectionFactory.abrir()) {
      inserir(c, acao);
    }
  }

  public void inserir(Connection c, Acao acao) throws SQLException {
    String sql = "INSERT INTO ACAO (NM_ACAO, NR_PONTOS) VALUES (?, ?)";
    try (PreparedStatement p = c.prepareStatement(sql, new String[] { "ID_ACAO" })) {
      p.setString(1, acao.getNome());
      p.setInt(2, acao.getPontos());
      p.executeUpdate();
      try (ResultSet keys = p.getGeneratedKeys()) {
        if (keys.next()) acao.setId(keys.getLong(1));
      }
    }
  }

  public List<Acao> listar() throws SQLException {
    List<Acao> lista = new ArrayList<>();
    try (
      Connection c = ConnectionFactory.abrir();
      Statement s = c.createStatement();
      ResultSet r = s.executeQuery(
        "SELECT ID_ACAO, NM_ACAO, NR_PONTOS FROM ACAO ORDER BY NR_PONTOS, NM_ACAO"
      )
    ) {
      while (r.next()) lista.add(mapear(r));
    }
    return lista;
  }

  public Acao buscarPorId(long id) throws SQLException {
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(
        "SELECT ID_ACAO, NM_ACAO, NR_PONTOS FROM ACAO WHERE ID_ACAO = ?"
      )
    ) {
      p.setLong(1, id);
      try (ResultSet r = p.executeQuery()) {
        return r.next() ? mapear(r) : null;
      }
    }
  }

  public Acao buscarPorNome(String nome) throws SQLException {
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(
        "SELECT ID_ACAO, NM_ACAO, NR_PONTOS FROM ACAO WHERE LOWER(NM_ACAO) = ?"
      )
    ) {
      p.setString(1, nome.trim().toLowerCase());
      try (ResultSet r = p.executeQuery()) {
        return r.next() ? mapear(r) : null;
      }
    }
  }

  public void atualizar(Acao acao) throws SQLException {
    String sql = "UPDATE ACAO SET NM_ACAO = ?, NR_PONTOS = ? WHERE ID_ACAO = ?";
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(sql)
    ) {
      p.setString(1, acao.getNome());
      p.setInt(2, acao.getPontos());
      p.setLong(3, acao.getId());
      p.executeUpdate();
    }
  }

  public void excluir(Connection c, long id) throws SQLException {
    try (PreparedStatement p = c.prepareStatement(
      "DELETE FROM ACAO WHERE ID_ACAO = ?"
    )) {
      p.setLong(1, id);
      p.executeUpdate();
    }
  }

  private Acao mapear(ResultSet r) throws SQLException {
    Acao acao = new Acao(r.getString("NM_ACAO"), r.getInt("NR_PONTOS"));
    acao.setId(r.getLong("ID_ACAO"));
    return acao;
  }
}
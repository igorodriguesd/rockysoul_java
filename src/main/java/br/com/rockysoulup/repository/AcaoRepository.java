package br.com.rockysoulup.repository;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.model.Acao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class AcaoRepository {

  public void inserir(Acao acao) throws SQLException {
    try (Connection con = ConnectionFactory.abrir()) {
      inserir(con, acao);
    }
  }

  // grava a ação aproveitando a conexão da transação
  public void inserir(Connection con, Acao acao) throws SQLException {
    String sql = "INSERT INTO ACAO (NM_ACAO, NR_PONTOS) VALUES (?, ?)";
    try (PreparedStatement pstmt = con.prepareStatement(sql, new String[] { "ID_ACAO" })) {
      pstmt.setString(1, acao.getNome());
      pstmt.setInt(2, acao.getPontos());
      pstmt.executeUpdate();
      try (ResultSet rs = pstmt.getGeneratedKeys()) {
        if (rs.next()) acao.setId(rs.getLong(1));
      }
    }
  }

  public List<Acao> listar() throws SQLException {
    String sql = "SELECT ID_ACAO, NM_ACAO, NR_PONTOS FROM ACAO ORDER BY NR_PONTOS, NM_ACAO";
    List<Acao> lista = new ArrayList<>();
    try (
      Connection con = ConnectionFactory.abrir();
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sql)
    ) {
      while (rs.next()) lista.add(mapear(rs));
    }
    return lista;
  }

  public Acao buscarPorId(long id) throws SQLException {
    String sql = "SELECT ID_ACAO, NM_ACAO, NR_PONTOS FROM ACAO WHERE ID_ACAO = ?";
    try (
      Connection con = ConnectionFactory.abrir();
      PreparedStatement pstmt = con.prepareStatement(sql)
    ) {
      pstmt.setLong(1, id);
      try (ResultSet rs = pstmt.executeQuery()) {
        return rs.next() ? mapear(rs) : null;
      }
    }
  }

  // evita cadastrar ação repetida (consulta por nome)
  public Acao buscarPorNome(String nome) throws SQLException {
    String sql = "SELECT ID_ACAO, NM_ACAO, NR_PONTOS FROM ACAO WHERE LOWER(NM_ACAO) = ?";
    try (
      Connection con = ConnectionFactory.abrir();
      PreparedStatement pstmt = con.prepareStatement(sql)
    ) {
      pstmt.setString(1, nome.trim().toLowerCase());
      try (ResultSet rs = pstmt.executeQuery()) {
        return rs.next() ? mapear(rs) : null;
      }
    }
  }

  public void atualizar(Acao acao) throws SQLException {
    String sql = "UPDATE ACAO SET NM_ACAO = ?, NR_PONTOS = ? WHERE ID_ACAO = ?";
    try (
      Connection con = ConnectionFactory.abrir();
      PreparedStatement pstmt = con.prepareStatement(sql)
    ) {
      pstmt.setString(1, acao.getNome());
      pstmt.setInt(2, acao.getPontos());
      pstmt.setLong(3, acao.getId());
      pstmt.executeUpdate();
    }
  }

  public void excluir(long id) throws SQLException {
    try (Connection con = ConnectionFactory.abrir()) {
      excluir(con, id);
    }
  }

  public void excluir(Connection con, long id) throws SQLException {
    String sql = "DELETE FROM ACAO WHERE ID_ACAO = ?";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setLong(1, id);
      pstmt.executeUpdate();
    }
  }

  // transforma o registro do banco em um objeto Acao
  private Acao mapear(ResultSet rs) throws SQLException {
    Acao acao = new Acao(rs.getString("NM_ACAO"), rs.getInt("NR_PONTOS"));
    acao.setId(rs.getLong("ID_ACAO"));
    return acao;
  }
}
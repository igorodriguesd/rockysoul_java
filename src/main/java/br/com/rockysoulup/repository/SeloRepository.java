package br.com.rockysoulup.repository;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.model.Selo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class SeloRepository {

  public void inserir(Selo selo) throws SQLException {
    try (Connection con = ConnectionFactory.abrir()) {
      inserir(con, selo);
    }
  }

  // grava o selo aproveitando a conexão da transação
  public void inserir(Connection con, Selo selo) throws SQLException {
    String sql = "INSERT INTO SELO (NM_SELO, DS_SELO, NR_PONTOS_MIN) VALUES (?, ?, ?)";
    try (PreparedStatement pstmt = con.prepareStatement(sql, new String[] { "ID_SELO" })) {
      pstmt.setString(1, selo.getNome());
      pstmt.setString(2, selo.getDescricao());
      pstmt.setInt(3, selo.getPontosMin());
      pstmt.executeUpdate();
      try (ResultSet rs = pstmt.getGeneratedKeys()) {
        if (rs.next()) selo.setId(rs.getLong(1));
      }
    }
  }

  public List<Selo> listar() throws SQLException {
    String sql = "SELECT ID_SELO, NM_SELO, DS_SELO, NR_PONTOS_MIN FROM SELO ORDER BY NR_PONTOS_MIN, ID_SELO";
    List<Selo> lista = new ArrayList<>();
    try (
      Connection con = ConnectionFactory.abrir();
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sql)
    ) {
      while (rs.next()) lista.add(mapear(rs));
    }
    return lista;
  }

  public Selo buscarPorId(long id) throws SQLException {
    String sql = "SELECT ID_SELO, NM_SELO, DS_SELO, NR_PONTOS_MIN FROM SELO WHERE ID_SELO = ?";
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

  public void atualizar(Selo selo) throws SQLException {
    String sql = "UPDATE SELO SET NM_SELO = ?, DS_SELO = ?, NR_PONTOS_MIN = ? WHERE ID_SELO = ?";
    try (
      Connection con = ConnectionFactory.abrir();
      PreparedStatement pstmt = con.prepareStatement(sql)
    ) {
      pstmt.setString(1, selo.getNome());
      pstmt.setString(2, selo.getDescricao());
      pstmt.setInt(3, selo.getPontosMin());
      pstmt.setLong(4, selo.getId());
      pstmt.executeUpdate();
    }
  }

  public void excluir(long id) throws SQLException {
    try (Connection con = ConnectionFactory.abrir()) {
      excluir(con, id);
    }
  }

  public void excluir(Connection con, long id) throws SQLException {
    String sql = "DELETE FROM SELO WHERE ID_SELO = ?";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setLong(1, id);
      pstmt.executeUpdate();
    }
  }

  // transforma o registro do banco em um objeto Selo
  private Selo mapear(ResultSet rs) throws SQLException {
    Selo selo = new Selo(
      rs.getString("NM_SELO"),
      rs.getString("DS_SELO"),
      rs.getInt("NR_PONTOS_MIN")
    );
    selo.setId(rs.getLong("ID_SELO"));
    return selo;
  }
}
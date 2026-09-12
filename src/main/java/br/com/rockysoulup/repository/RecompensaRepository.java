package br.com.rockysoulup.repository;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.model.Recompensa;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class RecompensaRepository {

  public void inserir(Recompensa recompensa) throws SQLException {
    try (Connection con = ConnectionFactory.abrir()) {
      inserir(con, recompensa);
    }
  }

  // grava a recompensa aproveitando a conexão da transação
  public void inserir(Connection con, Recompensa recompensa) throws SQLException {
    String sql =
      "INSERT INTO RECOMPENSA (NM_RECOMPENSA, DS_RECOMPENSA, NR_CUSTO, NR_ESTOQUE, DS_CATEGORIA, ST_DESTAQUE) VALUES (?, ?, ?, ?, ?, ?)";
    try (PreparedStatement pstmt = con.prepareStatement(
      sql, new String[] { "ID_RECOMPENSA" }
    )) {
      pstmt.setString(1, recompensa.getTitulo());
      pstmt.setString(2, recompensa.getDescricao());
      pstmt.setInt(3, recompensa.getCusto());
      pstmt.setInt(4, recompensa.getEstoque());
      pstmt.setString(5, recompensa.getCategoria());
      pstmt.setString(6, recompensa.getDestaque());
      pstmt.executeUpdate();
      try (ResultSet rs = pstmt.getGeneratedKeys()) {
        if (rs.next()) recompensa.setId(rs.getLong(1));
      }
    }
  }

  public List<Recompensa> listar() throws SQLException {
    String sql =
      "SELECT ID_RECOMPENSA, NM_RECOMPENSA, DS_RECOMPENSA, NR_CUSTO, NR_ESTOQUE, DS_CATEGORIA, ST_DESTAQUE FROM RECOMPENSA ORDER BY ID_RECOMPENSA";
    List<Recompensa> lista = new ArrayList<>();
    try (
      Connection con = ConnectionFactory.abrir();
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sql)
    ) {
      while (rs.next()) lista.add(mapear(rs));
    }
    return lista;
  }

  public Recompensa buscarPorId(long id) throws SQLException {
    String sql =
      "SELECT ID_RECOMPENSA, NM_RECOMPENSA, DS_RECOMPENSA, NR_CUSTO, NR_ESTOQUE, DS_CATEGORIA, ST_DESTAQUE FROM RECOMPENSA WHERE ID_RECOMPENSA = ?";
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

  // evita cadastrar recompensa repetida (consulta por título)
  public Recompensa buscarPorTitulo(String titulo) throws SQLException {
    String sql =
      "SELECT ID_RECOMPENSA, NM_RECOMPENSA, DS_RECOMPENSA, NR_CUSTO, NR_ESTOQUE, DS_CATEGORIA, ST_DESTAQUE FROM RECOMPENSA WHERE LOWER(NM_RECOMPENSA) = ?";
    try (
      Connection con = ConnectionFactory.abrir();
      PreparedStatement pstmt = con.prepareStatement(sql)
    ) {
      pstmt.setString(1, titulo.trim().toLowerCase());
      try (ResultSet rs = pstmt.executeQuery()) {
        return rs.next() ? mapear(rs) : null;
      }
    }
  }

  public void atualizar(Recompensa recompensa) throws SQLException {
    try (Connection con = ConnectionFactory.abrir()) {
      atualizar(con, recompensa);
    }
  }

  // altera a recompensa aproveitando a conexão da transação
  public void atualizar(Connection con, Recompensa recompensa) throws SQLException {
    String sql =
      "UPDATE RECOMPENSA SET NM_RECOMPENSA = ?, DS_RECOMPENSA = ?, NR_CUSTO = ?, NR_ESTOQUE = ?, DS_CATEGORIA = ?, ST_DESTAQUE = ? WHERE ID_RECOMPENSA = ?";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setString(1, recompensa.getTitulo());
      pstmt.setString(2, recompensa.getDescricao());
      pstmt.setInt(3, recompensa.getCusto());
      pstmt.setInt(4, recompensa.getEstoque());
      pstmt.setString(5, recompensa.getCategoria());
      pstmt.setString(6, recompensa.getDestaque());
      pstmt.setLong(7, recompensa.getId());
      pstmt.executeUpdate();
    }
  }

  public void excluir(long id) throws SQLException {
    try (Connection con = ConnectionFactory.abrir()) {
      excluir(con, id);
    }
  }

  public void excluir(Connection con, long id) throws SQLException {
    String sql = "DELETE FROM RECOMPENSA WHERE ID_RECOMPENSA = ?";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setLong(1, id);
      pstmt.executeUpdate();
    }
  }

  // transforma o registro do banco em um objeto Recompensa
  private Recompensa mapear(ResultSet rs) throws SQLException {
    Recompensa recompensa = new Recompensa(
      rs.getString("NM_RECOMPENSA"),
      rs.getString("DS_RECOMPENSA"),
      rs.getInt("NR_CUSTO"),
      rs.getInt("NR_ESTOQUE")
    );
    recompensa.setId(rs.getLong("ID_RECOMPENSA"));
    recompensa.setCategoria(rs.getString("DS_CATEGORIA"));
    recompensa.setDestaque(rs.getString("ST_DESTAQUE"));
    return recompensa;
  }
}
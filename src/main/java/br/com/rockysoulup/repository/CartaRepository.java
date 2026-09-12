package br.com.rockysoulup.repository;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.model.Carta;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class CartaRepository {

  public void inserir(Carta carta) throws SQLException {
    try (Connection con = ConnectionFactory.abrir()) {
      inserir(con, carta);
    }
  }

  // grava a carta aproveitando a conexão da transação
  public void inserir(Connection con, Carta carta) throws SQLException {
    String sql = "INSERT INTO CARTA (NM_CARTA, DS_CARTA, NM_CONJUNTO, DS_RARIDADE) VALUES (?, ?, ?, ?)";
    try (PreparedStatement pstmt = con.prepareStatement(sql, new String[] { "ID_CARTA" })) {
      pstmt.setString(1, carta.getNome());
      pstmt.setString(2, carta.getDescricao());
      pstmt.setString(3, carta.getConjunto());
      pstmt.setString(4, carta.getRaridade());
      pstmt.executeUpdate();
      try (ResultSet rs = pstmt.getGeneratedKeys()) {
        if (rs.next()) carta.setId(rs.getLong(1));
      }
    }
  }

  public List<Carta> listar() throws SQLException {
    String sql = "SELECT ID_CARTA, NM_CARTA, DS_CARTA, NM_CONJUNTO, DS_RARIDADE FROM CARTA ORDER BY NM_CONJUNTO, DS_RARIDADE, NM_CARTA";
    List<Carta> lista = new ArrayList<>();
    try (
      Connection con = ConnectionFactory.abrir();
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sql)
    ) {
      while (rs.next()) lista.add(mapear(rs));
    }
    return lista;
  }

  public Carta buscarPorId(long id) throws SQLException {
    String sql = "SELECT ID_CARTA, NM_CARTA, DS_CARTA, NM_CONJUNTO, DS_RARIDADE FROM CARTA WHERE ID_CARTA = ?";
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

  public void atualizar(Carta carta) throws SQLException {
    String sql = "UPDATE CARTA SET NM_CARTA = ?, DS_CARTA = ?, NM_CONJUNTO = ?, DS_RARIDADE = ? WHERE ID_CARTA = ?";
    try (
      Connection con = ConnectionFactory.abrir();
      PreparedStatement pstmt = con.prepareStatement(sql)
    ) {
      pstmt.setString(1, carta.getNome());
      pstmt.setString(2, carta.getDescricao());
      pstmt.setString(3, carta.getConjunto());
      pstmt.setString(4, carta.getRaridade());
      pstmt.setLong(5, carta.getId());
      pstmt.executeUpdate();
    }
  }

  public void excluir(long id) throws SQLException {
    try (Connection con = ConnectionFactory.abrir()) {
      excluir(con, id);
    }
  }

  public void excluir(Connection con, long id) throws SQLException {
    String sql = "DELETE FROM CARTA WHERE ID_CARTA = ?";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setLong(1, id);
      pstmt.executeUpdate();
    }
  }

  // transforma o registro do banco em um objeto Carta
  private Carta mapear(ResultSet rs) throws SQLException {
    Carta carta = new Carta(
      rs.getString("NM_CARTA"),
      rs.getString("DS_CARTA"),
      rs.getString("NM_CONJUNTO"),
      rs.getString("DS_RARIDADE")
    );
    carta.setId(rs.getLong("ID_CARTA"));
    return carta;
  }
}
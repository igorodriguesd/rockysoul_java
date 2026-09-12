package br.com.rockysoulup.repository;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.model.UsuarioCarta;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class UsuarioCartaRepository {

  // adiciona a carta à coleção; se já existir, soma a quantidade (sem quebrar a PK)
  public void inserir(UsuarioCarta usuarioCarta) throws SQLException {
    try (Connection con = ConnectionFactory.abrir()) {
      inserir(con, usuarioCarta);
    }
  }

  // adiciona a carta aproveitando a conexão da transação
  public void inserir(Connection con, UsuarioCarta usuarioCarta) throws SQLException {
    String sql =
      "MERGE INTO USUARIO_CARTA t " +
      "USING (SELECT ? ID_USUARIO, ? ID_CARTA FROM DUAL) s " +
      "ON (t.ID_USUARIO = s.ID_USUARIO AND t.ID_CARTA = s.ID_CARTA) " +
      "WHEN MATCHED THEN UPDATE SET t.QT_CARTA = t.QT_CARTA + ? " +
      "WHEN NOT MATCHED THEN INSERT (ID_USUARIO, ID_CARTA, QT_CARTA) VALUES (?, ?, ?)";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setLong(1, usuarioCarta.getUsuarioId());
      pstmt.setLong(2, usuarioCarta.getCartaId());
      pstmt.setInt(3, usuarioCarta.getQuantidade());
      pstmt.setLong(4, usuarioCarta.getUsuarioId());
      pstmt.setLong(5, usuarioCarta.getCartaId());
      pstmt.setInt(6, usuarioCarta.getQuantidade());
      pstmt.executeUpdate();
    }
  }

  public boolean jaPossui(Connection con, long usuarioId, long cartaId) throws SQLException {
    String sql = "SELECT 1 FROM USUARIO_CARTA WHERE ID_USUARIO = ? AND ID_CARTA = ?";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setLong(1, usuarioId);
      pstmt.setLong(2, cartaId);
      try (ResultSet rs = pstmt.executeQuery()) {
        return rs.next();
      }
    }
  }

  public List<UsuarioCarta> listarPorUsuario(long usuarioId) throws SQLException {
    String sql = "SELECT ID_USUARIO, ID_CARTA, QT_CARTA FROM USUARIO_CARTA WHERE ID_USUARIO = ? ORDER BY ID_CARTA";
    List<UsuarioCarta> lista = new ArrayList<>();
    try (
      Connection con = ConnectionFactory.abrir();
      PreparedStatement pstmt = con.prepareStatement(sql)
    ) {
      pstmt.setLong(1, usuarioId);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          lista.add(new UsuarioCarta(
            rs.getLong("ID_USUARIO"),
            rs.getLong("ID_CARTA"),
            rs.getInt("QT_CARTA")));
        }
      }
    }
    return lista;
  }

  public void atualizarQuantidade(Connection con, long usuarioId, long cartaId, int quantidade) throws SQLException {
    String sql = "UPDATE USUARIO_CARTA SET QT_CARTA = ? WHERE ID_USUARIO = ? AND ID_CARTA = ?";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setInt(1, quantidade);
      pstmt.setLong(2, usuarioId);
      pstmt.setLong(3, cartaId);
      pstmt.executeUpdate();
    }
  }

  // soma uma unidade à quantidade de uma carta repetida
  public void incrementarQuantidade(Connection con, long usuarioId, long cartaId) throws SQLException {
    String sql = "UPDATE USUARIO_CARTA SET QT_CARTA = QT_CARTA + 1 WHERE ID_USUARIO = ? AND ID_CARTA = ?";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setLong(1, usuarioId);
      pstmt.setLong(2, cartaId);
      pstmt.executeUpdate();
    }
  }

  // apaga as cartas de um usuário (usada na exclusão em cascata)
  public void excluirPorUsuario(Connection con, long usuarioId) throws SQLException {
    String sql = "DELETE FROM USUARIO_CARTA WHERE ID_USUARIO = ?";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setLong(1, usuarioId);
      pstmt.executeUpdate();
    }
  }

  // apaga as cartas do catálogo do acervo (usada na exclusão em cascata)
  public void excluirPorCarta(Connection con, long cartaId) throws SQLException {
    String sql = "DELETE FROM USUARIO_CARTA WHERE ID_CARTA = ?";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setLong(1, cartaId);
      pstmt.executeUpdate();
    }
  }
}
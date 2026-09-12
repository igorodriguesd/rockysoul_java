package br.com.rockysoulup.repository;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.model.UsuarioFragmento;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public final class UsuarioFragmentoRepository {

  public void inserir(Connection con, UsuarioFragmento frag) throws SQLException {
    String sql = "INSERT INTO USUARIO_FRAGMENTO (ID_USUARIO, DS_RARIDADE, QT_FRAGMENTOS) VALUES (?, ?, ?)";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setLong(1, frag.getUsuarioId());
      pstmt.setString(2, frag.getRaridade());
      pstmt.setInt(3, frag.getQuantidade());
      pstmt.executeUpdate();
    }
  }

  // soma fragmentos; cria a linha caso ainda não exista (MERGE = upsert)
  public void adicionar(Connection con, long usuarioId, String raridade, int quantidade) throws SQLException {
    String upsert =
      "MERGE INTO USUARIO_FRAGMENTO t " +
      "USING (SELECT ? ID_USUARIO, ? DS_RARIDADE FROM DUAL) s " +
      "ON (t.ID_USUARIO = s.ID_USUARIO AND t.DS_RARIDADE = s.DS_RARIDADE) " +
      "WHEN MATCHED THEN UPDATE SET t.QT_FRAGMENTOS = t.QT_FRAGMENTOS + ? " +
      "WHEN NOT MATCHED THEN INSERT (ID_USUARIO, DS_RARIDADE, QT_FRAGMENTOS) VALUES (?, ?, ?)";
    try (PreparedStatement pstmt = con.prepareStatement(upsert)) {
      pstmt.setLong(1, usuarioId);
      pstmt.setString(2, raridade);
      pstmt.setInt(3, quantidade);
      pstmt.setLong(4, usuarioId);
      pstmt.setString(5, raridade);
      pstmt.setInt(6, quantidade);
      pstmt.executeUpdate();
    }
  }

  public int buscar(Connection con, long usuarioId, String raridade) throws SQLException {
    String sql = "SELECT QT_FRAGMENTOS FROM USUARIO_FRAGMENTO WHERE ID_USUARIO = ? AND DS_RARIDADE = ?";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setLong(1, usuarioId);
      pstmt.setString(2, raridade);
      try (ResultSet rs = pstmt.executeQuery()) {
        return rs.next() ? rs.getInt("QT_FRAGMENTOS") : 0;
      }
    }
  }

  // retorna false quando o saldo de fragmentos é insuficiente
  public boolean gastar(Connection con, long usuarioId, String raridade, int quantidade) throws SQLException {
    int atual = buscar(con, usuarioId, raridade);
    if (atual < quantidade) return false;
    String sql = "UPDATE USUARIO_FRAGMENTO SET QT_FRAGMENTOS = QT_FRAGMENTOS - ? WHERE ID_USUARIO = ? AND DS_RARIDADE = ?";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setInt(1, quantidade);
      pstmt.setLong(2, usuarioId);
      pstmt.setString(3, raridade);
      pstmt.executeUpdate();
    }
    return true;
  }

  public Map<String, Integer> listarPorUsuario(Connection con, long usuarioId) throws SQLException {
    String sql = "SELECT DS_RARIDADE, QT_FRAGMENTOS FROM USUARIO_FRAGMENTO WHERE ID_USUARIO = ? ORDER BY DS_RARIDADE";
    Map<String, Integer> mapa = new LinkedHashMap<>();
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setLong(1, usuarioId);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          mapa.put(rs.getString("DS_RARIDADE"), rs.getInt("QT_FRAGMENTOS"));
        }
      }
    }
    return mapa;
  }

  // apaga os fragmentos de um usuário (usada na exclusão em cascata)
  public void excluirPorUsuario(Connection con, long usuarioId) throws SQLException {
    String sql = "DELETE FROM USUARIO_FRAGMENTO WHERE ID_USUARIO = ?";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setLong(1, usuarioId);
      pstmt.executeUpdate();
    }
  }
}
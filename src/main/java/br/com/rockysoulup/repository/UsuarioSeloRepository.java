package br.com.rockysoulup.repository;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.model.UsuarioSelo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class UsuarioSeloRepository {

  // vincula um selo conquistado ao usuário
  public void inserir(Connection con, UsuarioSelo relacao) throws SQLException {
    String sql = "INSERT INTO USUARIO_SELO (ID_USUARIO, ID_SELO) VALUES (?, ?)";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setLong(1, relacao.getUsuarioId());
      pstmt.setLong(2, relacao.getSeloId());
      pstmt.executeUpdate();
    }
  }

  // verifica se o usuário já conquistou o selo
  public boolean jaConquistado(long usuarioId, long seloId) throws SQLException {
    try (Connection con = ConnectionFactory.abrir()) {
      return jaConquistado(con, usuarioId, seloId);
    }
  }

  public boolean jaConquistado(Connection con, long usuarioId, long seloId)
    throws SQLException {
    String sql = "SELECT 1 FROM USUARIO_SELO WHERE ID_USUARIO = ? AND ID_SELO = ?";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setLong(1, usuarioId);
      pstmt.setLong(2, seloId);
      try (ResultSet rs = pstmt.executeQuery()) {
        return rs.next();
      }
    }
  }

  // desvincula o selo do usuário
  public void excluir(long usuarioId, long seloId) throws SQLException {
    String sql = "DELETE FROM USUARIO_SELO WHERE ID_USUARIO = ? AND ID_SELO = ?";
    try (
      Connection con = ConnectionFactory.abrir();
      PreparedStatement pstmt = con.prepareStatement(sql)
    ) {
      pstmt.setLong(1, usuarioId);
      pstmt.setLong(2, seloId);
      pstmt.executeUpdate();
    }
  }

  // desvincula todos os selos de um usuário
  public void excluirPorUsuario(Connection con, long usuarioId) throws SQLException {
    String sql = "DELETE FROM USUARIO_SELO WHERE ID_USUARIO = ?";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setLong(1, usuarioId);
      pstmt.executeUpdate();
    }
  }

  // desvincula um selo de todos os usuários
  public void excluirPorSelo(Connection con, long seloId) throws SQLException {
    String sql = "DELETE FROM USUARIO_SELO WHERE ID_SELO = ?";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setLong(1, seloId);
      pstmt.executeUpdate();
    }
  }

  // lista os selos já conquistados por um usuário
  public List<UsuarioSelo> listarPorUsuario(long usuarioId) throws SQLException {
    String sql =
      "SELECT ID_USUARIO, ID_SELO, DT_CONQUISTA FROM USUARIO_SELO WHERE ID_USUARIO = ? ORDER BY DT_CONQUISTA";
    List<UsuarioSelo> lista = new ArrayList<>();
    try (
      Connection con = ConnectionFactory.abrir();
      PreparedStatement pstmt = con.prepareStatement(sql)
    ) {
      pstmt.setLong(1, usuarioId);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          UsuarioSelo relacao = new UsuarioSelo(
            rs.getLong("ID_USUARIO"),
            rs.getLong("ID_SELO")
          );
          Date dt = rs.getDate("DT_CONQUISTA");
          if (dt != null) relacao.setDtConquista(dt.toLocalDate());
          lista.add(relacao);
        }
      }
    }
    return lista;
  }
}
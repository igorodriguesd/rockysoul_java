package br.com.rockysoulup.dao;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.model.UsuarioSelo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class UsuarioSeloDao {

  public void inserir(Connection c, UsuarioSelo relacao) throws SQLException {
    String sql =
      "INSERT INTO USUARIO_SELO (ID_USUARIO, ID_SELO) VALUES (?, ?)";
    try (PreparedStatement p = c.prepareStatement(sql)) {
      p.setLong(1, relacao.getUsuarioId());
      p.setLong(2, relacao.getSeloId());
      p.executeUpdate();
    }
  }

  public boolean jaConquistado(long usuarioId, long seloId)
    throws SQLException {
    try (Connection c = ConnectionFactory.abrir()) {
      return jaConquistado(c, usuarioId, seloId);
    }
  }

  public boolean jaConquistado(Connection c, long usuarioId, long seloId)
    throws SQLException {
    String sql =
      "SELECT 1 FROM USUARIO_SELO WHERE ID_USUARIO = ? AND ID_SELO = ?";
    try (PreparedStatement p = c.prepareStatement(sql)) {
      p.setLong(1, usuarioId);
      p.setLong(2, seloId);
      try (ResultSet r = p.executeQuery()) {
        return r.next();
      }
    }
  }

  public void excluir(long usuarioId, long seloId) throws SQLException {
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(
        "DELETE FROM USUARIO_SELO WHERE ID_USUARIO = ? AND ID_SELO = ?"
      )
    ) {
      p.setLong(1, usuarioId);
      p.setLong(2, seloId);
      p.executeUpdate();
    }
  }

  public void excluirPorUsuario(Connection c, long usuarioId) throws SQLException {
    try (PreparedStatement p = c.prepareStatement(
      "DELETE FROM USUARIO_SELO WHERE ID_USUARIO = ?"
    )) {
      p.setLong(1, usuarioId);
      p.executeUpdate();
    }
  }

  public void excluirPorSelo(Connection c, long seloId) throws SQLException {
    try (PreparedStatement p = c.prepareStatement(
      "DELETE FROM USUARIO_SELO WHERE ID_SELO = ?"
    )) {
      p.setLong(1, seloId);
      p.executeUpdate();
    }
  }

  public List<UsuarioSelo> listarPorUsuario(long usuarioId) throws SQLException {
    List<UsuarioSelo> lista = new ArrayList<>();
    String sql =
      "SELECT ID_USUARIO, ID_SELO, DT_CONQUISTA FROM USUARIO_SELO WHERE ID_USUARIO = ? ORDER BY DT_CONQUISTA";
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(sql)
    ) {
      p.setLong(1, usuarioId);
      try (ResultSet r = p.executeQuery()) {
        while (r.next()) {
          UsuarioSelo relacao = new UsuarioSelo(
            r.getLong("ID_USUARIO"),
            r.getLong("ID_SELO")
          );
          Date dt = r.getDate("DT_CONQUISTA");
          if (dt != null) relacao.setDtConquista(dt.toLocalDate());
          lista.add(relacao);
        }
      }
    }
    return lista;
  }
}
package br.com.rockysoulup.dao;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.model.Historico;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class HistoricoDao {

  public void inserir(Connection c, Historico historico) throws SQLException {
    String sql =
      "INSERT INTO HISTORICO (ID_USUARIO, DS_ACAO, NR_PONTOS) VALUES (?, ?, ?)";
    try (PreparedStatement p = c.prepareStatement(sql)) {
      p.setLong(1, historico.getUsuarioId());
      p.setString(2, historico.getDescricao());
      p.setInt(3, historico.getPontos());
      p.executeUpdate();
    }
  }

  public void atualizar(Historico historico) throws SQLException {
    String sql =
      "UPDATE HISTORICO SET DS_ACAO = ?, NR_PONTOS = ? WHERE ID_HISTORICO = ?";
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(sql)
    ) {
      p.setString(1, historico.getDescricao());
      p.setInt(2, historico.getPontos());
      p.setLong(3, historico.getId());
      p.executeUpdate();
    }
  }

  public void excluir(long id) throws SQLException {
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(
        "DELETE FROM HISTORICO WHERE ID_HISTORICO = ?"
      )
    ) {
      p.setLong(1, id);
      p.executeUpdate();
    }
  }

  public void excluirPorUsuario(Connection c, long usuarioId) throws SQLException {
    try (PreparedStatement p = c.prepareStatement(
      "DELETE FROM HISTORICO WHERE ID_USUARIO = ?"
    )) {
      p.setLong(1, usuarioId);
      p.executeUpdate();
    }
  }

  public List<Historico> listarPorUsuario(long usuarioId) throws SQLException {
    List<Historico> lista = new ArrayList<>();
    String sql =
      "SELECT ID_HISTORICO, ID_USUARIO, DS_ACAO, NR_PONTOS, DT_ACAO FROM HISTORICO WHERE ID_USUARIO = ? ORDER BY DT_ACAO DESC";
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(sql)
    ) {
      p.setLong(1, usuarioId);
      try (ResultSet r = p.executeQuery()) {
        while (r.next()) {
          Historico h = new Historico(
            r.getLong("ID_USUARIO"),
            r.getString("DS_ACAO"),
            r.getInt("NR_PONTOS")
          );
          h.setId(r.getLong("ID_HISTORICO"));
          Timestamp data = r.getTimestamp("DT_ACAO");
          if (data != null) h.setDataAcao(data.toLocalDateTime());
          lista.add(h);
        }
      }
    }
    return lista;
  }
}
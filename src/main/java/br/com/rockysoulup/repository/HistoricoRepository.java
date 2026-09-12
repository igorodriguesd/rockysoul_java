package br.com.rockysoulup.repository;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.model.Historico;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class HistoricoRepository {

  // registra uma ação no histórico do usuário
  public void inserir(Connection con, Historico historico) throws SQLException {
    String sql = "INSERT INTO HISTORICO (ID_USUARIO, DS_ACAO, NR_PONTOS) VALUES (?, ?, ?)";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setLong(1, historico.getUsuarioId());
      pstmt.setString(2, historico.getDescricao());
      pstmt.setInt(3, historico.getPontos());
      pstmt.executeUpdate();
    }
  }

  public void atualizar(Historico historico) throws SQLException {
    String sql = "UPDATE HISTORICO SET DS_ACAO = ?, NR_PONTOS = ? WHERE ID_HISTORICO = ?";
    try (
      Connection con = ConnectionFactory.abrir();
      PreparedStatement pstmt = con.prepareStatement(sql)
    ) {
      pstmt.setString(1, historico.getDescricao());
      pstmt.setInt(2, historico.getPontos());
      pstmt.setLong(3, historico.getId());
      pstmt.executeUpdate();
    }
  }

  public void excluir(long id) throws SQLException {
    String sql = "DELETE FROM HISTORICO WHERE ID_HISTORICO = ?";
    try (
      Connection con = ConnectionFactory.abrir();
      PreparedStatement pstmt = con.prepareStatement(sql)
    ) {
      pstmt.setLong(1, id);
      pstmt.executeUpdate();
    }
  }

  public Historico buscarPorId(long id) throws SQLException {
    String sql = "SELECT ID_HISTORICO, ID_USUARIO, DS_ACAO, NR_PONTOS, DT_ACAO FROM HISTORICO WHERE ID_HISTORICO = ?";
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

  // apaga todo o histórico de um usuário
  public void excluirPorUsuario(Connection con, long usuarioId) throws SQLException {
    String sql = "DELETE FROM HISTORICO WHERE ID_USUARIO = ?";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setLong(1, usuarioId);
      pstmt.executeUpdate();
    }
  }

  public List<Historico> listarPorUsuario(long usuarioId) throws SQLException {
    String sql = "SELECT ID_HISTORICO, ID_USUARIO, DS_ACAO, NR_PONTOS, DT_ACAO FROM HISTORICO WHERE ID_USUARIO = ? ORDER BY DT_ACAO DESC";
    List<Historico> lista = new ArrayList<>();
    try (
      Connection con = ConnectionFactory.abrir();
      PreparedStatement pstmt = con.prepareStatement(sql)
    ) {
      pstmt.setLong(1, usuarioId);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) lista.add(mapear(rs));
      }
    }
    return lista;
  }

  // transforma o registro do banco em um objeto Historico
  private Historico mapear(ResultSet rs) throws SQLException {
    Historico h = new Historico(
      rs.getLong("ID_USUARIO"),
      rs.getString("DS_ACAO"),
      rs.getInt("NR_PONTOS")
    );
    h.setId(rs.getLong("ID_HISTORICO"));
    Timestamp data = rs.getTimestamp("DT_ACAO");
    if (data != null) h.setDataAcao(data.toLocalDateTime());
    return h;
  }
}
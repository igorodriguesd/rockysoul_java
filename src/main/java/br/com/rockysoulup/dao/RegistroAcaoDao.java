package br.com.rockysoulup.dao;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.model.RegistroAcao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class RegistroAcaoDao {

  public void inserir(RegistroAcao registro) throws SQLException {
    String sql =
      "INSERT INTO REGISTRO_ACAO(ID_USUARIO,ID_ACAO,PONTOS_OBTIDOS) VALUES(?,?,?)";
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(sql)
    ) {
      p.setLong(1, registro.getUsuario().getId());
      p.setLong(2, registro.getAcao().getId());
      p.setInt(3, registro.getPontosObtidos());
      p.executeUpdate();
    }
  }

  public List<RegistroAcao> listarPorUsuario(long usuarioId)
    throws SQLException {
    List<RegistroAcao> lista = new ArrayList<>();
    String sql =
      "SELECT ID_REGISTRO,DATA_REGISTRO,PONTOS_OBTIDOS FROM REGISTRO_ACAO WHERE ID_USUARIO=? ORDER BY DATA_REGISTRO DESC";
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(sql)
    ) {
      p.setLong(1, usuarioId);
      try (ResultSet r = p.executeQuery()) {
        while (r.next()) {
          RegistroAcao registro = new RegistroAcao();
          registro.setId(r.getLong("ID_REGISTRO"));
          registro.setDataRegistro(
            r.getTimestamp("DATA_REGISTRO").toLocalDateTime()
          );
          registro.setPontosObtidos(r.getInt("PONTOS_OBTIDOS"));
          lista.add(registro);
        }
      }
    }
    return lista;
  }

  public void excluir(long id) throws SQLException {
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(
        "DELETE FROM REGISTRO_ACAO WHERE ID_REGISTRO=?"
      )
    ) {
      p.setLong(1, id);
      p.executeUpdate();
    }
  }
}

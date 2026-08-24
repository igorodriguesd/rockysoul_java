package br.com.rockysoulup.dao;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.model.Recompensa;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class RecompensaDao {

  public void inserir(Recompensa recompensa) throws SQLException {
    String sql =
      "INSERT INTO RECOMPENSA(TITULO,DESCRICAO,CUSTO,ESTOQUE) VALUES(?,?,?,?)";
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(sql)
    ) {
      p.setString(1, recompensa.getTitulo());
      p.setString(2, recompensa.getDescricao());
      p.setInt(3, recompensa.getCusto());
      p.setInt(4, recompensa.getEstoque());
      p.executeUpdate();
    }
  }

  public List<Recompensa> listar() throws SQLException {
    List<Recompensa> lista = new ArrayList<>();
    try (
      Connection c = ConnectionFactory.abrir();
      Statement s = c.createStatement();
      ResultSet r = s.executeQuery(
        "SELECT * FROM RECOMPENSA ORDER BY ID_RECOMPENSA"
      )
    ) {
      while (r.next()) lista.add(mapear(r));
    }
    return lista;
  }

  public Recompensa buscarPorId(long id) throws SQLException {
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(
        "SELECT * FROM RECOMPENSA WHERE ID_RECOMPENSA=?"
      )
    ) {
      p.setLong(1, id);
      try (ResultSet r = p.executeQuery()) {
        return r.next() ? mapear(r) : null;
      }
    }
  }

  public void atualizar(Recompensa recompensa) throws SQLException {
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(
        "UPDATE RECOMPENSA SET TITULO=?,DESCRICAO=?,CUSTO=?,ESTOQUE=? WHERE ID_RECOMPENSA=?"
      )
    ) {
      p.setString(1, recompensa.getTitulo());
      p.setString(2, recompensa.getDescricao());
      p.setInt(3, recompensa.getCusto());
      p.setInt(4, recompensa.getEstoque());
      p.setLong(5, recompensa.getId());
      p.executeUpdate();
    }
  }

  public void excluir(long id) throws SQLException {
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(
        "DELETE FROM RECOMPENSA WHERE ID_RECOMPENSA=?"
      )
    ) {
      p.setLong(1, id);
      p.executeUpdate();
    }
  }

  private Recompensa mapear(ResultSet r) throws SQLException {
    Recompensa x = new Recompensa(
      r.getString("TITULO"),
      r.getString("DESCRICAO"),
      r.getInt("CUSTO"),
      r.getInt("ESTOQUE")
    );
    x.setId(r.getLong("ID_RECOMPENSA"));
    return x;
  }
}

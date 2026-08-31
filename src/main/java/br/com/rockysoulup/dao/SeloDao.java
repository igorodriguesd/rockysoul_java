package br.com.rockysoulup.dao;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.model.Selo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class SeloDao {

  public void inserir(Selo selo) throws SQLException {
    try (Connection c = ConnectionFactory.abrir()) {
      inserir(c, selo);
    }
  }

  public void inserir(Connection c, Selo selo) throws SQLException {
    String sql =
      "INSERT INTO SELO (NM_SELO, DS_SELO, NR_PONTOS_MIN) VALUES (?, ?, ?)";
    try (PreparedStatement p = c.prepareStatement(sql, new String[] { "ID_SELO" })) {
      p.setString(1, selo.getNome());
      p.setString(2, selo.getDescricao());
      p.setInt(3, selo.getPontosMin());
      p.executeUpdate();
      try (ResultSet keys = p.getGeneratedKeys()) {
        if (keys.next()) selo.setId(keys.getLong(1));
      }
    }
  }

  public List<Selo> listar() throws SQLException {
    List<Selo> lista = new ArrayList<>();
    try (
      Connection c = ConnectionFactory.abrir();
      Statement s = c.createStatement();
      ResultSet r = s.executeQuery(
        "SELECT ID_SELO, NM_SELO, DS_SELO, NR_PONTOS_MIN FROM SELO ORDER BY NR_PONTOS_MIN, ID_SELO"
      )
    ) {
      while (r.next()) lista.add(mapear(r));
    }
    return lista;
  }

  public Selo buscarPorId(long id) throws SQLException {
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(
        "SELECT ID_SELO, NM_SELO, DS_SELO, NR_PONTOS_MIN FROM SELO WHERE ID_SELO = ?"
      )
    ) {
      p.setLong(1, id);
      try (ResultSet r = p.executeQuery()) {
        return r.next() ? mapear(r) : null;
      }
    }
  }

  public void atualizar(Selo selo) throws SQLException {
    String sql =
      "UPDATE SELO SET NM_SELO = ?, DS_SELO = ?, NR_PONTOS_MIN = ? WHERE ID_SELO = ?";
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(sql)
    ) {
      p.setString(1, selo.getNome());
      p.setString(2, selo.getDescricao());
      p.setInt(3, selo.getPontosMin());
      p.setLong(4, selo.getId());
      p.executeUpdate();
    }
  }

  public void excluir(long id) throws SQLException {
    try (Connection c = ConnectionFactory.abrir()) {
      excluir(c, id);
    }
  }

  public void excluir(Connection c, long id) throws SQLException {
    try (PreparedStatement p = c.prepareStatement(
      "DELETE FROM SELO WHERE ID_SELO = ?"
    )) {
      p.setLong(1, id);
      p.executeUpdate();
    }
  }

  private Selo mapear(ResultSet r) throws SQLException {
    Selo selo = new Selo(
      r.getString("NM_SELO"),
      r.getString("DS_SELO"),
      r.getInt("NR_PONTOS_MIN")
    );
    selo.setId(r.getLong("ID_SELO"));
    return selo;
  }
}
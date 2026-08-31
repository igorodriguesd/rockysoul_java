package br.com.rockysoulup.dao;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.model.Recompensa;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class RecompensaDao {

  public void inserir(Recompensa recompensa) throws SQLException {
    try (Connection c = ConnectionFactory.abrir()) {
      inserir(c, recompensa);
    }
  }

  public void inserir(Connection c, Recompensa recompensa) throws SQLException {
    String sql =
      "INSERT INTO RECOMPENSA (NM_RECOMPENSA, DS_RECOMPENSA, NR_CUSTO, NR_ESTOQUE, DS_CATEGORIA, ST_DESTAQUE) VALUES (?, ?, ?, ?, ?, ?)";
    try (PreparedStatement p = c.prepareStatement(
      sql, new String[] { "ID_RECOMPENSA" }
    )) {
      p.setString(1, recompensa.getTitulo());
      p.setString(2, recompensa.getDescricao());
      p.setInt(3, recompensa.getCusto());
      p.setInt(4, recompensa.getEstoque());
      p.setString(5, recompensa.getCategoria());
      p.setString(6, recompensa.getDestaque());
      p.executeUpdate();
      try (ResultSet keys = p.getGeneratedKeys()) {
        if (keys.next()) recompensa.setId(keys.getLong(1));
      }
    }
  }

  public List<Recompensa> listar() throws SQLException {
    List<Recompensa> lista = new ArrayList<>();
    try (
      Connection c = ConnectionFactory.abrir();
      Statement s = c.createStatement();
      ResultSet r = s.executeQuery(
        "SELECT ID_RECOMPENSA, NM_RECOMPENSA, DS_RECOMPENSA, NR_CUSTO, NR_ESTOQUE, DS_CATEGORIA, ST_DESTAQUE FROM RECOMPENSA ORDER BY ID_RECOMPENSA"
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
        "SELECT ID_RECOMPENSA, NM_RECOMPENSA, DS_RECOMPENSA, NR_CUSTO, NR_ESTOQUE, DS_CATEGORIA, ST_DESTAQUE FROM RECOMPENSA WHERE ID_RECOMPENSA = ?"
      )
    ) {
      p.setLong(1, id);
      try (ResultSet r = p.executeQuery()) {
        return r.next() ? mapear(r) : null;
      }
    }
  }

  public Recompensa buscarPorTitulo(String titulo) throws SQLException {
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(
        "SELECT ID_RECOMPENSA, NM_RECOMPENSA, DS_RECOMPENSA, NR_CUSTO, NR_ESTOQUE, DS_CATEGORIA, ST_DESTAQUE FROM RECOMPENSA WHERE LOWER(NM_RECOMPENSA) = ?"
      )
    ) {
      p.setString(1, titulo.trim().toLowerCase());
      try (ResultSet r = p.executeQuery()) {
        return r.next() ? mapear(r) : null;
      }
    }
  }

  public void atualizar(Recompensa recompensa) throws SQLException {
    String sql =
      "UPDATE RECOMPENSA SET NM_RECOMPENSA = ?, DS_RECOMPENSA = ?, NR_CUSTO = ?, NR_ESTOQUE = ?, DS_CATEGORIA = ?, ST_DESTAQUE = ? WHERE ID_RECOMPENSA = ?";
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(sql)
    ) {
      p.setString(1, recompensa.getTitulo());
      p.setString(2, recompensa.getDescricao());
      p.setInt(3, recompensa.getCusto());
      p.setInt(4, recompensa.getEstoque());
      p.setString(5, recompensa.getCategoria());
      p.setString(6, recompensa.getDestaque());
      p.setLong(7, recompensa.getId());
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
      "DELETE FROM RECOMPENSA WHERE ID_RECOMPENSA = ?"
    )) {
      p.setLong(1, id);
      p.executeUpdate();
    }
  }

  private Recompensa mapear(ResultSet r) throws SQLException {
    Recompensa recompensa = new Recompensa(
      r.getString("NM_RECOMPENSA"),
      r.getString("DS_RECOMPENSA"),
      r.getInt("NR_CUSTO"),
      r.getInt("NR_ESTOQUE")
    );
    recompensa.setId(r.getLong("ID_RECOMPENSA"));
    recompensa.setCategoria(r.getString("DS_CATEGORIA"));
    recompensa.setDestaque(r.getString("ST_DESTAQUE"));
    return recompensa;
  }
}
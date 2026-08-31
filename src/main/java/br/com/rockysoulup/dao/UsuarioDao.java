package br.com.rockysoulup.dao;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.model.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class UsuarioDao {

  public void inserir(Usuario usuario) throws SQLException {
    try (Connection c = ConnectionFactory.abrir()) {
      inserir(c, usuario);
    }
  }

  public void inserir(Connection c, Usuario usuario) throws SQLException {
    Objects.requireNonNull(usuario, "Usuário é obrigatório");
    String sql =
      "INSERT INTO USUARIO (NM_USUARIO, DS_EMAIL, NR_PONTOS, NR_PONTOS_RESGATADOS) VALUES (?, ?, ?, ?)";
    try (PreparedStatement p = c.prepareStatement(sql, new String[] { "ID_USUARIO" })) {
      p.setString(1, usuario.getNome());
      p.setString(2, usuario.getEmail());
      p.setInt(3, usuario.getPontos());
      p.setInt(4, usuario.getResgatados());
      p.executeUpdate();
      try (ResultSet keys = p.getGeneratedKeys()) {
        if (keys.next()) usuario.setId(keys.getLong(1));
      }
    }
  }

  public List<Usuario> listar() throws SQLException {
    List<Usuario> usuarios = new ArrayList<>();
    try (
      Connection c = ConnectionFactory.abrir();
      Statement s = c.createStatement();
      ResultSet r = s.executeQuery(
        "SELECT ID_USUARIO, NM_USUARIO, DS_EMAIL, NR_PONTOS, NR_PONTOS_RESGATADOS FROM USUARIO ORDER BY NR_PONTOS DESC, NM_USUARIO"
      )
    ) {
      while (r.next()) usuarios.add(mapear(r));
    }
    return usuarios;
  }

  public Usuario buscarPorId(long id) throws SQLException {
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(
        "SELECT ID_USUARIO, NM_USUARIO, DS_EMAIL, NR_PONTOS, NR_PONTOS_RESGATADOS FROM USUARIO WHERE ID_USUARIO = ?"
      )
    ) {
      p.setLong(1, id);
      try (ResultSet r = p.executeQuery()) {
        return r.next() ? mapear(r) : null;
      }
    }
  }

  public Usuario buscarPorEmail(String email) throws SQLException {
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(
        "SELECT ID_USUARIO, NM_USUARIO, DS_EMAIL, NR_PONTOS, NR_PONTOS_RESGATADOS FROM USUARIO WHERE LOWER(DS_EMAIL) = ?"
      )
    ) {
      p.setString(1, email.trim().toLowerCase());
      try (ResultSet r = p.executeQuery()) {
        return r.next() ? mapear(r) : null;
      }
    }
  }

  public void atualizar(Usuario usuario) throws SQLException {
    try (Connection c = ConnectionFactory.abrir()) {
      atualizar(c, usuario);
    }
  }

  public void atualizar(Connection c, Usuario usuario) throws SQLException {
    Objects.requireNonNull(usuario, "Usuário é obrigatório");
    String sql =
      "UPDATE USUARIO SET NM_USUARIO = ?, DS_EMAIL = ?, NR_PONTOS = ?, NR_PONTOS_RESGATADOS = ? WHERE ID_USUARIO = ?";
    try (PreparedStatement p = c.prepareStatement(sql)) {
      p.setString(1, usuario.getNome());
      p.setString(2, usuario.getEmail());
      p.setInt(3, usuario.getPontos());
      p.setInt(4, usuario.getResgatados());
      p.setLong(5, usuario.getId());
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
      "DELETE FROM USUARIO WHERE ID_USUARIO = ?"
    )) {
      p.setLong(1, id);
      p.executeUpdate();
    }
  }

  private Usuario mapear(ResultSet r) throws SQLException {
    Usuario usuario = new Usuario();
    usuario.setId(r.getLong("ID_USUARIO"));
    usuario.setNome(r.getString("NM_USUARIO"));
    usuario.setEmail(r.getString("DS_EMAIL"));
    usuario.setPontos(r.getInt("NR_PONTOS"));
    usuario.setResgatados(r.getInt("NR_PONTOS_RESGATADOS"));
    return usuario;
  }
}
package br.com.rockysoulup.dao;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.model.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class UsuarioDao {

  public void inserir(Usuario usuario) throws SQLException {
    Objects.requireNonNull(usuario, "Usuário é obrigatório");
    String sql =
      "INSERT INTO USUARIO (NOME, EMAIL, PONTOS, NIVEL) VALUES (?, ?, ?, ?)";
    try (
      Connection connection = ConnectionFactory.abrir();
      PreparedStatement statement = connection.prepareStatement(
        sql,
        new String[] { "ID_USUARIO" }
      )
    ) {
      statement.setString(1, usuario.getNome());
      statement.setString(2, usuario.getEmail());
      statement.setInt(3, usuario.getPontos());
      statement.setString(4, usuario.getNivel());
      statement.executeUpdate();
      try (ResultSet keys = statement.getGeneratedKeys()) {
        if (keys.next()) usuario.setId(keys.getLong(1));
      }
    }
  }

  public List<Usuario> listar() throws SQLException {
    List<Usuario> usuarios = new ArrayList<>();
    try (
      Connection connection = ConnectionFactory.abrir();
      Statement statement = connection.createStatement();
      ResultSet result = statement.executeQuery(
        "SELECT * FROM USUARIO ORDER BY ID_USUARIO"
      )
    ) {
      while (result.next()) usuarios.add(mapear(result));
    }
    return usuarios;
  }

  public Usuario buscarPorId(long id) throws SQLException {
    try (
      Connection connection = ConnectionFactory.abrir();
      PreparedStatement statement = connection.prepareStatement(
        "SELECT * FROM USUARIO WHERE ID_USUARIO = ?"
      )
    ) {
      statement.setLong(1, id);
      try (ResultSet result = statement.executeQuery()) {
        return result.next() ? mapear(result) : null;
      }
    }
  }

  public Usuario buscarPorEmail(String email) throws SQLException {
    try (
      Connection connection = ConnectionFactory.abrir();
      PreparedStatement statement = connection.prepareStatement(
        "SELECT * FROM USUARIO WHERE EMAIL = ?"
      )
    ) {
      statement.setString(1, email.trim().toLowerCase());
      try (ResultSet result = statement.executeQuery()) {
        return result.next() ? mapear(result) : null;
      }
    }
  }

  public void atualizar(Usuario usuario) throws SQLException {
    Objects.requireNonNull(usuario, "Usuário é obrigatório");
    String sql =
      "UPDATE USUARIO SET NOME = ?, EMAIL = ?, PONTOS = ?, NIVEL = ? WHERE ID_USUARIO = ?";
    try (
      Connection connection = ConnectionFactory.abrir();
      PreparedStatement statement = connection.prepareStatement(sql)
    ) {
      statement.setString(1, usuario.getNome());
      statement.setString(2, usuario.getEmail());
      statement.setInt(3, usuario.getPontos());
      statement.setString(4, usuario.getNivel());
      statement.setLong(5, usuario.getId());
      statement.executeUpdate();
    }
  }

  public void excluir(long id) throws SQLException {
    try (
      Connection connection = ConnectionFactory.abrir();
      PreparedStatement statement = connection.prepareStatement(
        "DELETE FROM USUARIO WHERE ID_USUARIO = ?"
      )
    ) {
      statement.setLong(1, id);
      statement.executeUpdate();
    }
  }

  private Usuario mapear(ResultSet result) throws SQLException {
    Usuario usuario = new Usuario();
    usuario.setId(result.getLong("ID_USUARIO"));
    usuario.setNome(result.getString("NOME"));
    usuario.setEmail(result.getString("EMAIL"));
    usuario.setPontos(result.getInt("PONTOS"));
    usuario.setNivel(result.getString("NIVEL"));
    return usuario;
  }
}

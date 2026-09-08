package br.com.rockysoulup.repository;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.model.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class UsuarioRepository {

  // grava um novo usuário
  public void inserir(Usuario usuario) throws SQLException {
    try (Connection con = ConnectionFactory.abrir()) {
      inserir(con, usuario);
    }
  }

  // grava o usuário aproveitando a conexão da transação
  public void inserir(Connection con, Usuario usuario) throws SQLException {
    Objects.requireNonNull(usuario, "Usuário é obrigatório");
    String sql =
      "INSERT INTO USUARIO (NM_USUARIO, DS_EMAIL, NR_PONTOS, NR_PONTOS_RESGATADOS) VALUES (?, ?, ?, ?)";
    try (PreparedStatement pstmt = con.prepareStatement(sql, new String[] { "ID_USUARIO" })) {
      pstmt.setString(1, usuario.getNome());
      pstmt.setString(2, usuario.getEmail());
      pstmt.setInt(3, usuario.getPontos());
      pstmt.setInt(4, usuario.getResgatados());
      pstmt.executeUpdate();
      try (ResultSet rs = pstmt.getGeneratedKeys()) {
        if (rs.next()) usuario.setId(rs.getLong(1));
      }
    }
  }

  // lista os usuários do ranking (mais pontos primeiro)
  public List<Usuario> listar() throws SQLException {
    String sql =
      "SELECT ID_USUARIO, NM_USUARIO, DS_EMAIL, NR_PONTOS, NR_PONTOS_RESGATADOS FROM USUARIO ORDER BY NR_PONTOS DESC, NM_USUARIO";
    List<Usuario> usuarios = new ArrayList<>();
    try (
      Connection con = ConnectionFactory.abrir();
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sql)
    ) {
      while (rs.next()) usuarios.add(mapear(rs));
    }
    return usuarios;
  }

  // procura um usuário pelo id
  public Usuario buscarPorId(long id) throws SQLException {
    String sql =
      "SELECT ID_USUARIO, NM_USUARIO, DS_EMAIL, NR_PONTOS, NR_PONTOS_RESGATADOS FROM USUARIO WHERE ID_USUARIO = ?";
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

  // procura um usuário pelo e-mail (regra do e-mail único)
  public Usuario buscarPorEmail(String email) throws SQLException {
    String sql =
      "SELECT ID_USUARIO, NM_USUARIO, DS_EMAIL, NR_PONTOS, NR_PONTOS_RESGATADOS FROM USUARIO WHERE LOWER(DS_EMAIL) = ?";
    try (
      Connection con = ConnectionFactory.abrir();
      PreparedStatement pstmt = con.prepareStatement(sql)
    ) {
      pstmt.setString(1, email.trim().toLowerCase());
      try (ResultSet rs = pstmt.executeQuery()) {
        return rs.next() ? mapear(rs) : null;
      }
    }
  }

  // altera os dados do usuário
  public void atualizar(Usuario usuario) throws SQLException {
    try (Connection con = ConnectionFactory.abrir()) {
      atualizar(con, usuario);
    }
  }

  public void atualizar(Connection con, Usuario usuario) throws SQLException {
    Objects.requireNonNull(usuario, "Usuário é obrigatório");
    String sql =
      "UPDATE USUARIO SET NM_USUARIO = ?, DS_EMAIL = ?, NR_PONTOS = ?, NR_PONTOS_RESGATADOS = ? WHERE ID_USUARIO = ?";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setString(1, usuario.getNome());
      pstmt.setString(2, usuario.getEmail());
      pstmt.setInt(3, usuario.getPontos());
      pstmt.setInt(4, usuario.getResgatados());
      pstmt.setLong(5, usuario.getId());
      pstmt.executeUpdate();
    }
  }

  // apaga um usuário do banco
  public void excluir(long id) throws SQLException {
    try (Connection con = ConnectionFactory.abrir()) {
      excluir(con, id);
    }
  }

  public void excluir(Connection con, long id) throws SQLException {
    String sql = "DELETE FROM USUARIO WHERE ID_USUARIO = ?";
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setLong(1, id);
      pstmt.executeUpdate();
    }
  }

  // transforma o registro do banco em um objeto Usuario
  private Usuario mapear(ResultSet rs) throws SQLException {
    Usuario usuario = new Usuario();
    usuario.setId(rs.getLong("ID_USUARIO"));
    usuario.setNome(rs.getString("NM_USUARIO"));
    usuario.setEmail(rs.getString("DS_EMAIL"));
    usuario.setPontos(rs.getInt("NR_PONTOS"));
    usuario.setResgatados(rs.getInt("NR_PONTOS_RESGATADOS"));
    return usuario;
  }
}
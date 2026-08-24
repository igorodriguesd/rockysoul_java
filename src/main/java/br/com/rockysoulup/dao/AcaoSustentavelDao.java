package br.com.rockysoulup.dao;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.model.AcaoSustentavel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AcaoSustentavelDao {

  public void inserir(AcaoSustentavel acao) throws SQLException {
    Objects.requireNonNull(acao, "Ação é obrigatória");
    String sql =
      "INSERT INTO ACAO_SUSTENTAVEL (NOME, DESCRICAO, PONTOS) VALUES (?, ?, ?)";
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(sql)
    ) {
      p.setString(1, acao.getNome());
      p.setString(2, acao.getDescricao());
      p.setInt(3, acao.getPontos());
      p.executeUpdate();
    }
  }

  public List<AcaoSustentavel> listar() throws SQLException {
    List<AcaoSustentavel> lista = new ArrayList<>();
    try (
      Connection c = ConnectionFactory.abrir();
      Statement s = c.createStatement();
      ResultSet r = s.executeQuery(
        "SELECT * FROM ACAO_SUSTENTAVEL ORDER BY ID_ACAO"
      )
    ) {
      while (r.next()) {
        AcaoSustentavel a = new AcaoSustentavel(
          r.getString("NOME"),
          r.getString("DESCRICAO"),
          r.getInt("PONTOS")
        );
        a.setId(r.getLong("ID_ACAO"));
        lista.add(a);
      }
    }
    return lista;
  }

  public void atualizar(AcaoSustentavel acao) throws SQLException {
    Objects.requireNonNull(acao, "Ação é obrigatória");
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(
        "UPDATE ACAO_SUSTENTAVEL SET NOME=?, DESCRICAO=?, PONTOS=? WHERE ID_ACAO=?"
      )
    ) {
      p.setString(1, acao.getNome());
      p.setString(2, acao.getDescricao());
      p.setInt(3, acao.getPontos());
      p.setLong(4, acao.getId());
      p.executeUpdate();
    }
  }

  public void excluir(long id) throws SQLException {
    try (
      Connection c = ConnectionFactory.abrir();
      PreparedStatement p = c.prepareStatement(
        "DELETE FROM ACAO_SUSTENTAVEL WHERE ID_ACAO=?"
      )
    ) {
      p.setLong(1, id);
      p.executeUpdate();
    }
  }
}

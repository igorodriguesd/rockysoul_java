package br.com.rockysoulup.repository;

import br.com.rockysoulup.connection.ConnectionFactory;
import br.com.rockysoulup.model.UsuarioCarta;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class UsuarioCartaRepository {

    public void inserir(UsuarioCarta usuarioCarta) throws SQLException {
        try (Connection con = ConnectionFactory.abrir()) {
            inserir(con, usuarioCarta);
        }
    }

    public void inserir(Connection con, UsuarioCarta usuarioCarta) throws SQLException {
        String sql = "INSERT INTO USUARIO_CARTA (ID_USUARIO, ID_CARTA, QT_CARTA, ST_BRILHANTE) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setLong(1, usuarioCarta.getUsuarioId());
            pstmt.setLong(2, usuarioCarta.getCartaId());
            pstmt.setInt(3, usuarioCarta.getQuantidade());
            pstmt.setString(4, usuarioCarta.isBrilhante() ? "S" : "N");
            pstmt.executeUpdate();
        }
    }

    public boolean jaPossui(Connection con, long usuarioId, long cartaId) throws SQLException {
        String sql = "SELECT 1 FROM USUARIO_CARTA WHERE ID_USUARIO = ? AND ID_CARTA = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setLong(1, usuarioId);
            pstmt.setLong(2, cartaId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<UsuarioCarta> listarPorUsuario(long usuarioId) throws SQLException {
        String sql = "SELECT ID_USUARIO, ID_CARTA, QT_CARTA, ST_BRILHANTE FROM USUARIO_CARTA WHERE ID_USUARIO = ? ORDER BY ID_CARTA";
        List<UsuarioCarta> lista = new ArrayList<>();
        try (
                Connection con = ConnectionFactory.abrir();
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setLong(1, usuarioId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UsuarioCarta uc = new UsuarioCarta(
                            rs.getLong("ID_USUARIO"),
                            rs.getLong("ID_CARTA"),
                            rs.getInt("QT_CARTA"),
                            "S".equalsIgnoreCase(rs.getString("ST_BRILHANTE")));
                    lista.add(uc);
                }
            }
        }
        return lista;
    }

    public void atualizarQuantidade(Connection con, long usuarioId, long cartaId, int quantidade) throws SQLException {
        String sql = "UPDATE USUARIO_CARTA SET QT_CARTA = ? WHERE ID_USUARIO = ? AND ID_CARTA = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, quantidade);
            pstmt.setLong(2, usuarioId);
            pstmt.setLong(3, cartaId);
            pstmt.executeUpdate();
        }
    }

    public void excluirPorUsuario(Connection con, long usuarioId) throws SQLException {
        String sql = "DELETE FROM USUARIO_CARTA WHERE ID_USUARIO = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setLong(1, usuarioId);
            pstmt.executeUpdate();
        }
    }
}

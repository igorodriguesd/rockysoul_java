package br.com.rockysoulup.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionFactory {

  private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
  private static final String USER = "ROCKY";
  private static final String PASSWORD = "senha";

  private ConnectionFactory() {}

  public static Connection abrir() throws SQLException {
    return DriverManager.getConnection(URL, USER, PASSWORD);
  }
}

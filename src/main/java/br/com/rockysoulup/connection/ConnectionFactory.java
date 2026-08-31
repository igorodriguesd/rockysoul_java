package br.com.rockysoulup.connection;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Abre conexoes JDBC com o Oracle.
 *
 * <p>As credenciais de acesso ao banco estao inseridas no proprio codigo
 * (constantes abaixo), conforme a rubrica da entrega. Variaveis de ambiente
 * e o arquivo db.properties podem sobrepor esses valores quando definidos:
 * <ol>
 *   <li>Variaveis de ambiente DB_URL, DB_USER e DB_PASSWORD;</li>
 *   <li>Arquivo db.properties (no classpath);</li>
 *   <li>Valores padrao definidos no codigo.</li>
 * </ol>
 */
public final class ConnectionFactory {

  private static final String URL_PADRAO = "jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL";
  private static final String USUARIO_PADRAO = "rm570651";
  private static final String SENHA_PADRAO = "260399";

  private ConnectionFactory() {}

  private static String url() {
    return valor("DB_URL", "DB_URL", URL_PADRAO);
  }

  private static String usuario() {
    return valor("DB_USER", "DB_USER", USUARIO_PADRAO);
  }

  private static String senha() {
    return valor("DB_PASSWORD", "DB_PASSWORD", SENHA_PADRAO);
  }

  private static String valor(String variavelAmbiente, String chaveArquivo, String padrao) {
    String doAmbiente = System.getenv(variavelAmbiente);
    if (doAmbiente != null && !doAmbiente.isBlank()) return doAmbiente.trim();

    Properties configuracoes = carregarArquivo();
    String doArquivo = configuracoes.getProperty(chaveArquivo);
    if (doArquivo != null && !doArquivo.isBlank()) return doArquivo.trim();

    return padrao;
  }

  private static Properties carregarArquivo() {
    Properties configuracoes = new Properties();
    try (InputStream entrada = ConnectionFactory.class.getResourceAsStream("/db.properties")) {
      if (entrada != null) configuracoes.load(entrada);
    } catch (IOException e) {
      // Sem arquivo de configuracao: usa variaveis de ambiente ou os padroes.
    }
    return configuracoes;
  }

  public static Connection abrir() throws SQLException {
    try {
      return DriverManager.getConnection(url(), usuario(), senha());
    } catch (SQLException e) {
      if (naoConfigurado(usuario()) || naoConfigurado(senha())) {
        throw new SQLException(
          "Banco nao configurado: se nao usou as credenciais padrão do código, "
            + "preencha DB_USER e DB_PASSWORD em db.properties "
            + "(ou defina as variaveis de ambiente DB_URL, DB_USER e DB_PASSWORD).",
          e
        );
      }
      throw e;
    }
  }

  private static boolean naoConfigurado(String valor) {
    return valor == null || valor.isBlank() || valor.startsWith("troque_");
  }

  public static String endereco() {
    return url() + " usuario=" + usuario();
  }
}
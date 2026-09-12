package br.com.rockysoulup.exception;

/**
 * Lançada quando o cadastro tenta usar um dado único que já existe, como
 * um e-mail já cadastrado.
 */
public class RegistroDuplicadoException extends Exception {

  private static final long serialVersionUID = 1L;

  public RegistroDuplicadoException(String mensagem) {
    super(mensagem);
  }

  public RegistroDuplicadoException(String mensagem, Throwable causa) {
    super(mensagem, causa);
  }
}
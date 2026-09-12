package br.com.rockysoulup.exception;

/**
 * Lançada quando o usuário tenta uma operação sem saldo suficiente:
 * pontos para resgatar uma recompensa ou fragmentos para fabricar uma carta.
 */
public class SaldoInsuficienteException extends Exception {

  private static final long serialVersionUID = 1L;

  public SaldoInsuficienteException(String mensagem) {
    super(mensagem);
  }

  public SaldoInsuficienteException(String mensagem, Throwable causa) {
    super(mensagem, causa);
  }
}
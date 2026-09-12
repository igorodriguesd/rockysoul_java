package br.com.rockysoulup.model;

import java.util.Objects;

/** Fragmentos de raridade acumulados pelo usuário para fabricar cartas. */
public class UsuarioFragmento {

  private Long usuarioId;
  private String raridade;
  private int quantidade;

  public UsuarioFragmento() {}

  public UsuarioFragmento(Long usuarioId, String raridade, int quantidade) {
    setUsuarioId(usuarioId);
    setRaridade(raridade);
    setQuantidade(quantidade);
  }

  public Long getUsuarioId() {
    return usuarioId;
  }

  public void setUsuarioId(Long usuarioId) {
    if (usuarioId == null || usuarioId <= 0) throw new IllegalArgumentException(
      "Usuário é obrigatório"
    );
    this.usuarioId = usuarioId;
  }

  public String getRaridade() {
    return raridade;
  }

  public void setRaridade(String raridade) {
    String normalizada = Carta.normalizarRaridade(raridade);
    if (!Carta.raridadeValida(normalizada)) {
      throw new IllegalArgumentException("Raridade inválida: " + raridade);
    }
    this.raridade = normalizada;
  }

  public int getQuantidade() {
    return quantidade;
  }

  public void setQuantidade(int quantidade) {
    if (quantidade < 0) throw new IllegalArgumentException(
      "Quantidade não pode ser negativa"
    );
    this.quantidade = quantidade;
  }

  @Override
  public String toString() {
    return "UsuarioFragmento{usuarioId=" + usuarioId + ", raridade='" + raridade + "', quantidade=" + quantidade + "}";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    UsuarioFragmento other = (UsuarioFragmento) obj;
    return Objects.equals(usuarioId, other.usuarioId)
      && Objects.equals(raridade, other.raridade);
  }

  @Override
  public int hashCode() {
    return Objects.hash(usuarioId, raridade);
  }
}
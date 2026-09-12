package br.com.rockysoulup.model;

import java.util.Objects;

/** Associação entre usuário e coleção de cartas. */
public class UsuarioCarta {

  private Long usuarioId;
  private Long cartaId;
  private int quantidade;

  public UsuarioCarta() {}

  public UsuarioCarta(Long usuarioId, Long cartaId) {
    this(usuarioId, cartaId, 1);
  }

  public UsuarioCarta(Long usuarioId, Long cartaId, int quantidade) {
    setUsuarioId(usuarioId);
    setCartaId(cartaId);
    setQuantidade(quantidade);
  }

  public Long getUsuarioId() {
    return usuarioId;
  }

  public void setUsuarioId(Long usuarioId) {
    if (usuarioId == null || usuarioId <= 0) throw new IllegalArgumentException(
      "Usuário da carta é obrigatório"
    );
    this.usuarioId = usuarioId;
  }

  public Long getCartaId() {
    return cartaId;
  }

  public void setCartaId(Long cartaId) {
    if (cartaId == null || cartaId <= 0) throw new IllegalArgumentException(
      "Carta é obrigatória"
    );
    this.cartaId = cartaId;
  }

  public int getQuantidade() {
    return quantidade;
  }

  public void setQuantidade(int quantidade) {
    if (quantidade <= 0) throw new IllegalArgumentException(
      "Quantidade da carta deve ser maior que zero"
    );
    this.quantidade = quantidade;
  }

  @Override
  public String toString() {
    return "UsuarioCarta{usuarioId=" + usuarioId + ", cartaId=" + cartaId + ", quantidade=" + quantidade + "}";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    UsuarioCarta other = (UsuarioCarta) obj;
    return Objects.equals(usuarioId, other.usuarioId)
      && Objects.equals(cartaId, other.cartaId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(usuarioId, cartaId);
  }
}
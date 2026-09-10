package br.com.rockysoulup.model;

/** Associação entre usuário e coleção de cartas. */
public class UsuarioCarta {

    private Long usuarioId;
    private Long cartaId;
    private int quantidade;
    private boolean brilhante;

    public UsuarioCarta() {
    }

    public UsuarioCarta(Long usuarioId, Long cartaId) {
        this(usuarioId, cartaId, 1, false);
    }

    public UsuarioCarta(Long usuarioId, Long cartaId, int quantidade, boolean brilhante) {
        setUsuarioId(usuarioId);
        setCartaId(cartaId);
        setQuantidade(quantidade);
        setBrilhante(brilhante);
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        if (usuarioId == null || usuarioId <= 0)
            throw new IllegalArgumentException(
                    "Usuário da carta é obrigatório");
        this.usuarioId = usuarioId;
    }

    public Long getCartaId() {
        return cartaId;
    }

    public void setCartaId(Long cartaId) {
        if (cartaId == null || cartaId <= 0)
            throw new IllegalArgumentException(
                    "Carta é obrigatória");
        this.cartaId = cartaId;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        if (quantidade <= 0)
            throw new IllegalArgumentException(
                    "Quantidade da carta deve ser maior que zero");
        this.quantidade = quantidade;
    }

    public boolean isBrilhante() {
        return brilhante;
    }

    public void setBrilhante(boolean brilhante) {
        this.brilhante = brilhante;
    }
}

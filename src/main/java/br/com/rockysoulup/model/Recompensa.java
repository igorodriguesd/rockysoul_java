package br.com.rockysoulup.model;

import java.util.Objects;

/** Recompensa resgatável por pontos, com estoque limitado. */
public class Recompensa {

  private static final int MAX_TITULO = 100;
  private static final int MAX_DESCRICAO = 200;
  private static final int MAX_CATEGORIA = 30;
  private static final int MAX_DESTAQUE = 20;

  private Long id;
  private String titulo;
  private String descricao;
  private int custo;
  private int estoque;
  private String categoria;
  private String destaque;

  public Recompensa() {}

  public Recompensa(String titulo, String descricao, int custo, int estoque) {
    this(titulo, descricao, custo, estoque, "", "");
  }

  public Recompensa(
    String titulo,
    String descricao,
    int custo,
    int estoque,
    String categoria,
    String destaque
  ) {
    setTitulo(titulo);
    setDescricao(descricao);
    setCusto(custo);
    setEstoque(estoque);
    setCategoria(categoria);
    setDestaque(destaque);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    if (id != null && id <= 0) throw new IllegalArgumentException(
      "Id da recompensa inválido"
    );
    this.id = id;
  }

  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String titulo) {
    if (titulo == null || titulo.isBlank()) throw new IllegalArgumentException(
      "O título da recompensa é obrigatório"
    );
    String limpo = titulo.trim();
    if (limpo.length() > MAX_TITULO) throw new IllegalArgumentException(
      "O título deve ter no máximo " + MAX_TITULO + " caracteres"
    );
    this.titulo = limpo;
  }

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    String limpo = descricao == null ? "" : descricao.trim();
    if (limpo.length() > MAX_DESCRICAO) throw new IllegalArgumentException(
      "A descrição deve ter no máximo " + MAX_DESCRICAO + " caracteres"
    );
    this.descricao = limpo;
  }

  public int getCusto() {
    return custo;
  }

  public void setCusto(int custo) {
    if (custo <= 0) throw new IllegalArgumentException(
      "O custo da recompensa deve ser positivo"
    );
    this.custo = custo;
  }

  public int getEstoque() {
    return estoque;
  }

  public void setEstoque(int estoque) {
    if (estoque < 0) throw new IllegalArgumentException(
      "O estoque não pode ser negativo"
    );
    this.estoque = estoque;
  }

  public String getCategoria() {
    return categoria;
  }

  public void setCategoria(String categoria) {
    String limpo = categoria == null ? "" : categoria.trim();
    if (limpo.length() > MAX_CATEGORIA) throw new IllegalArgumentException(
      "A categoria deve ter no máximo " + MAX_CATEGORIA + " caracteres"
    );
    this.categoria = limpo;
  }

  public String getDestaque() {
    return destaque;
  }

  public void setDestaque(String destaque) {
    String limpo = destaque == null ? "" : destaque.trim();
    if (limpo.length() > MAX_DESTAQUE) throw new IllegalArgumentException(
      "O destaque deve ter no máximo " + MAX_DESTAQUE + " caracteres"
    );
    this.destaque = limpo;
  }

  @Override
  public String toString() {
    return "Recompensa{id=" + id + ", titulo='" + titulo + "', custo=" + custo + ", estoque=" + estoque + "}";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Recompensa other = (Recompensa) obj;
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
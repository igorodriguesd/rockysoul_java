package br.com.rockysoulup.model;

/** Recompensa resgatável por pontos, com estoque limitado. */
public class Recompensa {

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
    this.titulo = titulo.trim();
  }

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    this.descricao = descricao == null ? "" : descricao.trim();
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
    this.categoria = categoria == null ? "" : categoria.trim();
  }

  public String getDestaque() {
    return destaque;
  }

  public void setDestaque(String destaque) {
    this.destaque = destaque == null ? "" : destaque.trim();
  }
}
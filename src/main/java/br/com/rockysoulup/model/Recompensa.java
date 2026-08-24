package br.com.rockysoulup.model;

public class Recompensa {

  private Long id;
  private String titulo;
  private String descricao;
  private int custo;
  private int estoque;

  public Recompensa() {}

  public Recompensa(String titulo, String descricao, int custo, int estoque) {
    setTitulo(titulo);
    setDescricao(descricao);
    setCusto(custo);
    setEstoque(estoque);
  }

  /** Informa se a recompensa ainda pode ser resgatada. */
  public boolean possuiEstoque() {
    return estoque > 0;
  }

  /** Reduz uma unidade do estoque após um resgate válido. */
  public void reduzirEstoque() {
    if (!possuiEstoque()) {
      throw new IllegalStateException("Recompensa sem estoque");
    }
    estoque--;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String titulo) {
    if (titulo == null || titulo.isBlank()) throw new IllegalArgumentException(
      "Título é obrigatório"
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
      "Custo deve ser positivo"
    );
    this.custo = custo;
  }

  public int getEstoque() {
    return estoque;
  }

  public void setEstoque(int estoque) {
    if (estoque < 0) throw new IllegalArgumentException(
      "Estoque não pode ser negativo"
    );
    this.estoque = estoque;
  }
}

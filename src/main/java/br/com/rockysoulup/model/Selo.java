package br.com.rockysoulup.model;

/** Selo conquistado automaticamente ao atingir determinada pontuação. */
public class Selo {

  private Long id;
  private String nome;
  private String descricao;
  private int pontosMin;

  public Selo() {}

  public Selo(String nome, String descricao, int pontosMin) {
    setNome(nome);
    setDescricao(descricao);
    setPontosMin(pontosMin);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    if (id != null && id <= 0) throw new IllegalArgumentException(
      "Id do selo inválido"
    );
    this.id = id;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    if (nome == null || nome.isBlank()) throw new IllegalArgumentException(
      "Nome do selo é obrigatório"
    );
    this.nome = nome.trim();
  }

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    this.descricao = descricao == null ? "" : descricao.trim();
  }

  public int getPontosMin() {
    return pontosMin;
  }

  public void setPontosMin(int pontosMin) {
    if (pontosMin < 0) throw new IllegalArgumentException(
      "A pontuação mínima não pode ser negativa"
    );
    this.pontosMin = pontosMin;
  }
}
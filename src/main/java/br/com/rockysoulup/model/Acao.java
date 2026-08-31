package br.com.rockysoulup.model;

/** Ação sustentável cadastrada no catálogo do sistema. */
public class Acao {

  private Long id;
  private String nome;
  private int pontos;

  public Acao() {}

  public Acao(String nome, int pontos) {
    setNome(nome);
    setPontos(pontos);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    if (id != null && id <= 0) throw new IllegalArgumentException(
      "Id da ação inválido"
    );
    this.id = id;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    if (nome == null || nome.isBlank()) throw new IllegalArgumentException(
      "O nome da ação é obrigatório"
    );
    this.nome = nome.trim();
  }

  public int getPontos() {
    return pontos;
  }

  public void setPontos(int pontos) {
    if (pontos <= 0 || pontos > 100) throw new IllegalArgumentException(
      "Os pontos da ação devem estar entre 1 e 100"
    );
    this.pontos = pontos;
  }
}
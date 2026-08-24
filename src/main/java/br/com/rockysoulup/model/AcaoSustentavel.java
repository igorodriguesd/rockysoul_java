package br.com.rockysoulup.model;

public class AcaoSustentavel {

  private Long id;
  private String nome;
  private String descricao;
  private int pontos;

  public AcaoSustentavel() {}

  public AcaoSustentavel(String nome, String descricao, int pontos) {
    setNome(nome);
    setDescricao(descricao);
    setPontos(pontos);
  }

  /** Confirma que a ação possui uma pontuação válida. */
  public boolean validarPontos() {
    return pontos > 0;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    if (nome == null || nome.isBlank() || nome.trim().isBlank()) throw new IllegalArgumentException(
      "Nome é obrigatório"
    );
    this.nome = nome.trim();
  }

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    this.descricao = descricao == null ? "" : descricao.trim();
  }

  public int getPontos() {
    return pontos;
  }

  /** Atualiza a pontuação impedindo valores nulos ou negativos. */
  public void setPontos(int pontos) {
    if (pontos <= 0) throw new IllegalArgumentException(
      "A ação deve valer pontos positivos"
    );
    this.pontos = pontos;
  }
}

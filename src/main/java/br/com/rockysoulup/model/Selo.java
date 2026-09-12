package br.com.rockysoulup.model;

import java.util.Objects;

/** Selo conquistado automaticamente ao atingir determinada pontuação. */
public class Selo {

  private static final int MAX_NOME = 100;
  private static final int MAX_DESCRICAO = 200;

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
    String limpo = nome.trim();
    if (limpo.length() > MAX_NOME) throw new IllegalArgumentException(
      "O nome do selo deve ter no máximo " + MAX_NOME + " caracteres"
    );
    this.nome = limpo;
  }

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    String limpo = descricao == null ? "" : descricao.trim();
    if (limpo.length() > MAX_DESCRICAO) throw new IllegalArgumentException(
      "A descrição do selo deve ter no máximo " + MAX_DESCRICAO + " caracteres"
    );
    this.descricao = limpo;
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

  @Override
  public String toString() {
    return "Selo{id=" + id + ", nome='" + nome + "', pontosMin=" + pontosMin + "}";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Selo other = (Selo) obj;
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
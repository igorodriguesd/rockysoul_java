package br.com.rockysoulup.model;

import java.util.Objects;

/** Ação sustentável cadastrada no catálogo do sistema. */
public class Acao {

  private static final int MAX_NOME = 100;

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
    String limpo = nome.trim();
    if (limpo.length() > MAX_NOME) throw new IllegalArgumentException(
      "O nome da ação deve ter no máximo " + MAX_NOME + " caracteres"
    );
    this.nome = limpo;
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

  @Override
  public String toString() {
    return "Acao{id=" + id + ", nome='" + nome + "', pontos=" + pontos + "}";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Acao other = (Acao) obj;
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
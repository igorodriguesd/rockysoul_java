package br.com.rockysoulup.model;

import java.time.LocalDateTime;

/** Registro de uma ação sustentável executada por um usuário. */
public class Historico {

  private Long id;
  private Long usuarioId;
  private String descricao;
  private int pontos;
  private LocalDateTime dataAcao;

  public Historico() {}

  public Historico(Long usuarioId, String descricao, int pontos) {
    setUsuarioId(usuarioId);
    setDescricao(descricao);
    setPontos(pontos);
    this.dataAcao = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    if (id != null && id <= 0) throw new IllegalArgumentException(
      "Id do histórico inválido"
    );
    this.id = id;
  }

  public Long getUsuarioId() {
    return usuarioId;
  }

  public void setUsuarioId(Long usuarioId) {
    if (usuarioId == null || usuarioId <= 0) throw new IllegalArgumentException(
      "O histórico precisa de um usuário"
    );
    this.usuarioId = usuarioId;
  }

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    if (descricao == null || descricao.isBlank()) throw new IllegalArgumentException(
      "Descrição da ação é obrigatória"
    );
    this.descricao = descricao.trim();
  }

  public int getPontos() {
    return pontos;
  }

  public void setPontos(int pontos) {
    if (pontos < 0 || pontos > 100) throw new IllegalArgumentException(
      "A pontuação da ação deve estar entre 0 e 100"
    );
    this.pontos = pontos;
  }

  public LocalDateTime getDataAcao() {
    return dataAcao;
  }

  public void setDataAcao(LocalDateTime dataAcao) {
    this.dataAcao = dataAcao;
  }
}
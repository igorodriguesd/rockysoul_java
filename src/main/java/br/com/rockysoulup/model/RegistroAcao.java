package br.com.rockysoulup.model;

import java.time.LocalDateTime;

public class RegistroAcao {

  private Long id;
  private Usuario usuario;
  private AcaoSustentavel acao;
  private LocalDateTime dataRegistro;
  private int pontosObtidos;

  public RegistroAcao() {}

  public RegistroAcao(Usuario usuario, AcaoSustentavel acao) {
    if (usuario == null || acao == null) throw new IllegalArgumentException(
      "Usuário e ação são obrigatórios"
    );
    this.usuario = usuario;
    this.acao = acao;
    this.dataRegistro = LocalDateTime.now();
    this.pontosObtidos = acao.getPontos();
  }

  /** Obtém a pontuação atual da ação registrada. */
  public int calcularPontos() {
    return acao == null ? 0 : acao.getPontos();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public AcaoSustentavel getAcao() {
    return acao;
  }

  public void setAcao(AcaoSustentavel acao) {
    this.acao = acao;
  }

  public LocalDateTime getDataRegistro() {
    return dataRegistro;
  }

  public void setDataRegistro(LocalDateTime data) {
    this.dataRegistro = data;
  }

  public int getPontosObtidos() {
    return pontosObtidos;
  }

  public void setPontosObtidos(int pontos) {
    if (pontos <= 0) throw new IllegalArgumentException(
      "Pontos obtidos devem ser positivos"
    );
    this.pontosObtidos = pontos;
  }
}

package br.com.rockysoulup.model;

import java.time.LocalDate;

/** Associação N:N entre usuário e selo conquistado. */
public class UsuarioSelo {

  private Long usuarioId;
  private Long seloId;
  private LocalDate dtConquista;

  public UsuarioSelo() {}

  public UsuarioSelo(Long usuarioId, Long seloId) {
    setUsuarioId(usuarioId);
    setSeloId(seloId);
    this.dtConquista = LocalDate.now();
  }

  public Long getUsuarioId() {
    return usuarioId;
  }

  public void setUsuarioId(Long usuarioId) {
    if (usuarioId == null || usuarioId <= 0) throw new IllegalArgumentException(
      "Usuário é obrigatório"
    );
    this.usuarioId = usuarioId;
  }

  public Long getSeloId() {
    return seloId;
  }

  public void setSeloId(Long seloId) {
    if (seloId == null || seloId <= 0) throw new IllegalArgumentException(
      "Selo é obrigatório"
    );
    this.seloId = seloId;
  }

  public LocalDate getDtConquista() {
    return dtConquista;
  }

  public void setDtConquista(LocalDate dtConquista) {
    this.dtConquista = dtConquista;
  }
}
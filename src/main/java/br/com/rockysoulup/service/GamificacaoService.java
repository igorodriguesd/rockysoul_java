package br.com.rockysoulup.service;

import br.com.rockysoulup.model.Selo;
import br.com.rockysoulup.model.Usuario;

public final class GamificacaoService {

  /** Credita os pontos de uma ação ao usuário, aplicando as regras de nível. */
  public void registrarAcao(Usuario usuario, int pontos) {
    validarObrigatorio(usuario, "Usuário");
    usuario.adicionarPontos(pontos);
  }

  /** Verifica se o usuário já atingiu a pontuação mínima do selo. */
  public boolean seloConquistado(Usuario usuario, Selo selo) {
    validarObrigatorio(usuario, "Usuário");
    validarObrigatorio(selo, "Selo");
    return usuario.getPontos() >= selo.getPontosMin();
  }

  private void validarObrigatorio(Object objeto, String nome) {
    if (objeto == null) throw new IllegalArgumentException(
      nome + " é obrigatório"
    );
  }
}
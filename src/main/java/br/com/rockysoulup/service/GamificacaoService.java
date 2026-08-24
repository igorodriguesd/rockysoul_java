package br.com.rockysoulup.service;

import br.com.rockysoulup.model.AcaoSustentavel;
import br.com.rockysoulup.model.Recompensa;
import br.com.rockysoulup.model.Usuario;

public final class GamificacaoService {

  /** Registra uma ação e credita seus pontos ao usuário. */
  public void registrarAcao(Usuario usuario, AcaoSustentavel acao) {
    validarObrigatorio(usuario, "Usuário");
    validarObrigatorio(acao, "Ação sustentável");
    usuario.adicionarPontos(acao.getPontos());
  }

  /** Verifica saldo suficiente e disponibilidade em estoque. */
  public boolean podeResgatar(Usuario usuario, Recompensa recompensa) {
    validarObrigatorio(usuario, "Usuário");
    validarObrigatorio(recompensa, "Recompensa");
    return (
      usuario.getPontos() >= recompensa.getCusto() && recompensa.possuiEstoque()
    );
  }

  /** Executa o resgate debitando pontos e reduzindo o estoque. */
  public void resgatar(Usuario usuario, Recompensa recompensa) {
    if (!podeResgatar(usuario, recompensa)) throw new IllegalStateException(
      "Saldo ou estoque insuficiente"
    );
    usuario.removerPontos(recompensa.getCusto());
    usuario.registrarResgate(recompensa.getCusto());
    recompensa.reduzirEstoque();
  }

  private void validarObrigatorio(Object objeto, String nome) {
    if (objeto == null) throw new IllegalArgumentException(
      nome + " é obrigatório"
    );
  }
}

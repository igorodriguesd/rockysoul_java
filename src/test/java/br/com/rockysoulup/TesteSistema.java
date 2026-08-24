package br.com.rockysoulup;

import br.com.rockysoulup.model.*;
import br.com.rockysoulup.service.GamificacaoService;

public class TesteSistema {

  public static void main(String[] args) {
    Usuario usuario = new Usuario("Igor", "igor@email.com");
    AcaoSustentavel reciclagem = new AcaoSustentavel(
      "Reciclagem",
      "Separar resíduos",
      30
    );
    GamificacaoService service = new GamificacaoService();
    service.registrarAcao(usuario, reciclagem);
    System.out.println(usuario);
    usuario.adicionarPontos(100);
    System.out.println("Nível: " + usuario.getNivel());
    Recompensa recompensa = new Recompensa(
      "Plantar uma árvore",
      "Muda nativa",
      100,
      2
    );
    service.resgatar(usuario, recompensa);
    System.out.println("Saldo após resgate: " + usuario.getPontos());
  }
}

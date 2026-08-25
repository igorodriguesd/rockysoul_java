package br.com.rockysoulup;

import static org.junit.jupiter.api.Assertions.*;

import br.com.rockysoulup.model.*;
import br.com.rockysoulup.service.GamificacaoService;
import org.junit.jupiter.api.Test;

class GamificacaoServiceTest {

  private final GamificacaoService service = new GamificacaoService();

  @Test
  void deveAdicionarPontosEAtualizarNivel() {
    Usuario u = new Usuario("Ana", "ana@email.com");
    service.registrarAcao(u, new AcaoSustentavel("Reciclagem", "Resíduos", 30));
    assertEquals(30, u.getPontos());
    assertEquals("SEMENTE", u.getNivel());
  }

  @Test
  void deveCalcularRankingConformePontos() {
    Usuario u = new Usuario("Ana", "ana@email.com");
    assertEquals("SEMENTE", u.calcularNivel());
    u.adicionarPontos(99);
    assertEquals("SEMENTE", u.getNivel());
    u.adicionarPontos(1);
    assertEquals("BROTO", u.getNivel());
    u.adicionarPontos(200);
    assertEquals("ÁRVORE", u.getNivel());
    u.adicionarPontos(300);
    assertEquals("EXPERT", u.getNivel());
  }

  @Test
  void deveResgatarRecompensa() {
    Usuario u = new Usuario("Ana", "ana@email.com");
    u.adicionarPontos(200);
    Recompensa r = new Recompensa("Árvore", "Muda", 100, 1);
    service.resgatar(u, r);
    assertEquals(100, u.getPontos());
    assertEquals(100, u.getPontosResgatados());
    assertEquals(0, r.getEstoque());
  }

  @Test
  void naoDeveResgatarSemSaldo() {
    Usuario u = new Usuario("Ana", "ana@email.com");
    Recompensa r = new Recompensa("Árvore", "Muda", 100, 1);
    assertThrows(IllegalStateException.class, () -> service.resgatar(u, r));
  }

  @Test
  void deveCalcularProgressoDeNivel() {
    Usuario u = new Usuario("Ana", "ana@email.com");
    assertEquals("BROTO", u.proximoNivel());
    assertEquals(100, u.pontosParaProximoNivel());
    u.adicionarPontos(600);
    assertNull(u.proximoNivel());
    assertEquals(-1, u.pontosParaProximoNivel());
  }
}

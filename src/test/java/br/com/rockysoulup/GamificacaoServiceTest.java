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
    service.registrarAcao(u, 30);
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
  void deveConquistarSeloQuandoAtingirMinimo() {
    Usuario u = new Usuario("Ana", "ana@email.com");
    Selo broto = new Selo("Broto", "50 pontos", 50);
    assertFalse(service.seloConquistado(u, broto));
    u.adicionarPontos(50);
    assertTrue(service.seloConquistado(u, broto));
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

  @Test
  void deveValidarRegrasDoModelo() {
    assertThrows(
      IllegalArgumentException.class,
      () -> new Usuario("", "ana@email.com")
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> new Usuario("Ana", "email-invalido")
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> new Historico(1L, "Ação", 200)
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> new Selo("S", "D", -1)
    );
  }
}
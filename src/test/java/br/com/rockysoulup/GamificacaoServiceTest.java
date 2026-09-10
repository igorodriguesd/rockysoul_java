package br.com.rockysoulup;

import static org.junit.jupiter.api.Assertions.*;

import br.com.rockysoulup.model.*;
import br.com.rockysoulup.service.GamificacaoService;
import br.com.rockysoulup.service.RockySoulService;
import org.junit.jupiter.api.Test;

import java.util.List;

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
  void deveCalcularFragmentosConformeRaridade() {
    Carta comum = new Carta("Reciclagem", "Descrição", "Recursos", "COMUM");
    Carta rara = new Carta("Energia Limpa", "Descrição", "Energia Limpa", "RARA");
    Carta lendaria = new Carta("Estrela do Futuro", "Descrição", "Cultivo", "LENDARIA");

    assertEquals(3, comum.getFragmentosPorRaridade());
    assertEquals(10, rara.getFragmentosPorRaridade());
    assertEquals(40, lendaria.getFragmentosPorRaridade());
  }

  @Test
  void deveUsarChanceDeDropPorRaridade() {
    Carta comum = new Carta("Reciclagem", "Descrição", "Recursos", "COMUM");
    Carta incomum = new Carta("Água Viva", "Descrição", "Guardiões da Água", "INCOMUM");
    Carta lendaria = new Carta("Muda do Amanhã", "Descrição", "Cultivo", "LENDARIA");

    assertEquals(75, comum.getChanceDeDrop());
    assertEquals(55, incomum.getChanceDeDrop());
    assertEquals(10, lendaria.getChanceDeDrop());
    assertEquals("COMUM", Carta.sortearRaridadeAleatoria(10));
    assertEquals("LENDARIA", Carta.sortearRaridadeAleatoria(100));
  }

  @Test
  void deveValidarRegrasDoModeloDeCarta() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Carta("", "Descrição", "Recursos", "COMUM"));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Carta("Carta", "Descrição", "", "COMUM"));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Carta("Carta", "Descrição", "Recursos", "RARIDADE_INVALIDA"));
  }

  @Test
  void deveListarCatalogoPadraoDeCartas() {
    List<Carta> catalogo = List.of(
        new Carta("Água Viva", "Recursos hídricos", "Guardiões da Água", "INCOMUM"),
        new Carta("Sol Forte", "Energia limpa", "Energia Limpa", "EPICA"),
        new Carta("Muda do Amanhã", "Cultivo sustentável", "Cultivo", "LENDARIA"));

    assertEquals(3, catalogo.size());
    assertEquals("INCOMUM", catalogo.get(0).getRaridade());
    assertEquals("LENDARIA", catalogo.get(2).getRaridade());
  }

  @Test
  void deveTerCatalogoCompletoInspiradoNoReact() {
    List<Carta> catalogo = new RockySoulService().catalogoPadrao();

    assertEquals(20, catalogo.size());
    assertTrue(catalogo.stream().anyMatch(c -> c.getNome().equals("Reciclagem")));
    assertTrue(catalogo.stream().anyMatch(c -> c.getNome().equals("Agrofloresta")));
    assertTrue(catalogo.stream().anyMatch(c -> c.getNome().equals("Economia de Água")));
    assertTrue(catalogo.stream().anyMatch(c -> c.getNome().equals("Energia Solar")));
  }

  @Test
  void deveValidarRegrasDoModelo() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Usuario("", "ana@email.com"));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Usuario("Ana", "email-invalido"));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Historico(1L, "Ação", 200));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Selo("S", "D", -1));
  }
}
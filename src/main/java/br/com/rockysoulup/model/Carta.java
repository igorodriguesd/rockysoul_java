package br.com.rockysoulup.model;

import java.text.Normalizer;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/** Carta ecológica colecionável, com conjunto e raridade próprios. */
public class Carta {

  private static final int MAX_NOME = 100;
  private static final int MAX_DESCRICAO = 200;
  private static final int MAX_CONJUNTO = 60;
  private static final int MAX_RARIDADE = 30;

  private static final Set<String> RARIDADES_VALIDAS = Set.of(
    "COMUM", "INCOMUM", "RARA", "EPICA", "LENDARIA"
  );

  private Long id;
  private String nome;
  private String descricao;
  private String conjunto;
  private String raridade;

  public Carta() {}

  public Carta(String nome, String descricao, String conjunto, String raridade) {
    setNome(nome);
    setDescricao(descricao);
    setConjunto(conjunto);
    setRaridade(raridade);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    if (id != null && id <= 0) throw new IllegalArgumentException(
      "Id da carta inválido"
    );
    this.id = id;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    if (nome == null || nome.isBlank()) throw new IllegalArgumentException(
      "O nome da carta é obrigatório"
    );
    String limpo = nome.trim();
    if (limpo.length() > MAX_NOME) throw new IllegalArgumentException(
      "O nome da carta deve ter no máximo " + MAX_NOME + " caracteres"
    );
    this.nome = limpo;
  }

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    String limpo = descricao == null ? "" : descricao.trim();
    if (limpo.length() > MAX_DESCRICAO) throw new IllegalArgumentException(
      "A descrição da carta deve ter no máximo " + MAX_DESCRICAO + " caracteres"
    );
    this.descricao = limpo;
  }

  public String getConjunto() {
    return conjunto;
  }

  public void setConjunto(String conjunto) {
    if (conjunto == null || conjunto.isBlank()) throw new IllegalArgumentException(
      "O conjunto da carta é obrigatório"
    );
    String limpo = conjunto.trim();
    if (limpo.length() > MAX_CONJUNTO) throw new IllegalArgumentException(
      "O conjunto deve ter no máximo " + MAX_CONJUNTO + " caracteres"
    );
    this.conjunto = limpo;
  }

  public String getRaridade() {
    return raridade;
  }

  public void setRaridade(String raridade) {
    String normalizada = normalizarRaridade(raridade);
    if (!raridadeValida(normalizada)) {
      throw new IllegalArgumentException("Raridade inválida: " + raridade);
    }
    this.raridade = normalizada;
  }

  /** Sorteia uma raridade conforme a probabilidade do catálogo. */
  public static String sortearRaridadeAleatoria() {
    int valor = ThreadLocalRandom.current().nextInt(1, 101);
    return sortearRaridadeAleatoria(valor);
  }

  /** Sorteio por faixa acumulada: 75% COMUM, 15% INCOMUM, 6% RARA, 3% EPICA, 1% LENDARIA. */
  public static String sortearRaridadeAleatoria(int valorSorteado) {
    if (valorSorteado < 1 || valorSorteado > 100) {
      throw new IllegalArgumentException("O valor sorteado deve estar entre 1 e 100");
    }
    if (valorSorteado <= 75) return "COMUM";
    if (valorSorteado <= 90) return "INCOMUM";
    if (valorSorteado <= 96) return "RARA";
    if (valorSorteado <= 99) return "EPICA";
    return "LENDARIA";
  }

  /** Normaliza a raridade: sem acentos, maiúsculas e sem espaços/traços. */
  public static String normalizarRaridade(String valor) {
    if (valor == null || valor.isBlank()) return "";
    String semAcentos = Normalizer
      .normalize(valor.trim(), Normalizer.Form.NFD)
      .replaceAll("\\p{M}", "");
    return semAcentos.toUpperCase().replace(" ", "_").replace("-", "_");
  }

  /** Informa se a raridade normalizada é uma das válidas do sistema. */
  public static boolean raridadeValida(String normalizada) {
    return RARIDADES_VALIDAS.contains(normalizada);
  }

  @Override
  public String toString() {
    return "Carta{id=" + id + ", nome='" + nome + "', conjunto='" + conjunto + "', raridade='" + raridade + "'}";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Carta other = (Carta) obj;
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
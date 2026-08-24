package br.com.rockysoulup.model;

import java.util.Objects;

public class Usuario {

  private static final String[] NOMES_NIVEIS = {
    "RAIZ DE NADA",
    "BROTO",
    "GRAVETO",
    "ERVA",
    "MATO ALTO",
    "TRONCO",
    "JARDINEIRO DO EDEN",
  };
  private static final int[] PONTOS_MINIMOS = {100, 200, 300, 400, 500, 600};

  private Long id;
  private String nome;
  private String email;
  private int pontos;
  private int pontosResgatados;
  private String nivel;

  public Usuario() {
    this.nivel = calcularNivel();
  }

  public Usuario(String nome, String email) {
    this();
    setNome(nome);
    setEmail(email);
  }

  /** Adiciona pontos ao usuário e recalcula o nível. */
  public void adicionarPontos(int valor) {
    validarValorPositivo(valor, "A quantidade de pontos deve ser positiva");
    pontos += valor;
    nivel = calcularNivel();
  }

  /** Remove pontos durante o resgate de uma recompensa. */
  public void removerPontos(int valor) {
    validarValorPositivo(valor, "A quantidade de pontos deve ser positiva");
    if (valor > pontos) throw new IllegalArgumentException(
      "Saldo insuficiente"
    );
    pontos -= valor;
    nivel = calcularNivel();
  }

  /** Acumula o total de pontos convertidos em recompensas. */
  public void registrarResgate(int valor) {
    validarValorPositivo(valor, "A quantidade de pontos deve ser positiva");
    pontosResgatados += valor;
  }

  /** Determina o nível conforme a faixa de pontuação acumulada. */
  public String calcularNivel() {
    String atual = NOMES_NIVEIS[0];
    for (int i = 0; i < PONTOS_MINIMOS.length; i++) {
      if (pontos >= PONTOS_MINIMOS[i]) atual = NOMES_NIVEIS[i + 1];
    }
    return atual;
  }

  /** Pontos que faltam para o próximo nível (-1 quando já é o máximo). */
  public int pontosParaProximoNivel() {
    for (int i = 0; i < NOMES_NIVEIS.length - 1; i++) {
      if (NOMES_NIVEIS[i].equals(calcularNivel())) {
        return PONTOS_MINIMOS[i] - pontos;
      }
    }
    return -1;
  }

  /** Nome do próximo nível (null quando já é o máximo). */
  public String proximoNivel() {
    for (int i = 0; i < NOMES_NIVEIS.length - 1; i++) {
      if (NOMES_NIVEIS[i].equals(calcularNivel())) {
        return NOMES_NIVEIS[i + 1];
      }
    }
    return null;
  }

  private void validarValorPositivo(int valor, String mensagem) {
    if (valor <= 0) throw new IllegalArgumentException(mensagem);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    if (nome == null || nome.isBlank()) throw new IllegalArgumentException(
      "Nome é obrigatório"
    );
    this.nome = nome.trim();
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    if (
      email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    ) throw new IllegalArgumentException("E-mail inválido");
    this.email = email.trim().toLowerCase();
  }

  public int getPontos() {
    return pontos;
  }

  public void setPontos(int pontos) {
    if (pontos < 0) throw new IllegalArgumentException(
      "Pontos não podem ser negativos"
    );
    this.pontos = pontos;
    this.nivel = calcularNivel();
  }

  public int getPontosResgatados() {
    return pontosResgatados;
  }

  public void setPontosResgatados(int valor) {
    if (valor < 0) throw new IllegalArgumentException(
      "Pontos resgatados não podem ser negativos"
    );
    this.pontosResgatados = valor;
  }

  public String getNivel() {
    return nivel;
  }

  public void setNivel(String nivel) {
    this.nivel = Objects.requireNonNull(nivel, "Nível é obrigatório");
  }

  @Override
  public String toString() {
    return (
      "Usuario{id=" +
      id +
      ", nome='" +
      nome +
      "', pontos=" +
      pontos +
      ", nivel='" +
      nivel +
      "'}"
    );
  }
}

package br.com.rockysoulup.repository;

import br.com.rockysoulup.model.AcaoSustentavel;
import br.com.rockysoulup.model.Recompensa;
import br.com.rockysoulup.model.RegistroAcao;
import br.com.rockysoulup.model.Usuario;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class JsonDatabase {

  private static final Path ARQUIVO = Path.of(
    System.getProperty("user.home"),
    ".rockysoulup",
    "data.json"
  );

  public static Path caminhoDoArquivo() {
    return ARQUIVO;
  }

  private final Gson gson = new GsonBuilder()
    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
    .setPrettyPrinting()
    .create();

  private Dados dados = new Dados();

  public JsonDatabase() {
    carregar();
    normalizar();
    semearAcoesPadrao();
    semearRecompensasPadrao();
  }

  private void semearRecompensasPadrao() {
    String[][] padroes = {
      {"Plantar 1 Árvore", "Muda nativa plantada por você", "200", "50"},
      {"Kit Reciclagem", "Kit de reciclagem doméstico", "250", "20"},
      {"Crédito Transporte", "R$ 20 de transporte público", "300", "15"},
      {"Cupom Mercado Orgânico", "R$ 30 em produtos orgânicos", "350", "10"},
      {"Aluguel de Bicicleta", "1 semana de bicicleta grátis", "400", "10"},
      {"Desconto na Energia", "10% de desconto na fatura", "500", "5"},
      {"Adotar uma Área Verde", "Parque adotado por 1 mês", "1500", "2"},
      {"Mês de Energia Grátis", "Conta de luz zerada por 1 mês", "2000", "1"},
    };
    boolean alterou = false;
    for (String[] p : padroes) {
      boolean existe = dados.recompensas
        .stream()
        .anyMatch(r -> r.getTitulo().equalsIgnoreCase(p[0]));
      if (!existe) {
        Recompensa recompensa = new Recompensa(
          p[0],
          p[1],
          Integer.parseInt(p[2]),
          Integer.parseInt(p[3])
        );
        recompensa.setId(++dados.seqRecompensa);
        dados.recompensas.add(recompensa);
        alterou = true;
      }
    }
    if (alterou) persistir();
  }

  private void normalizar() {
    Set<Long> ids = new HashSet<>();
    Set<String> chaves = new HashSet<>();
    List<Usuario> usuariosUnicos = new ArrayList<>();
    for (Usuario u : dados.usuarios) {
      if (u.getId() == null || !ids.add(u.getId())) continue;
      String chave =
        u.getNome().toLowerCase() + "|" + u.getEmail().toLowerCase();
      if (!chaves.add(chave)) continue;
      u.setPontos(u.getPontos());
      usuariosUnicos.add(u);
    }
    dados.usuarios = usuariosUnicos;

    ids.clear();
    chaves.clear();
    List<AcaoSustentavel> acoesUnicas = new ArrayList<>();
    for (AcaoSustentavel a : dados.acoes) {
      if (a.getId() == null || !ids.add(a.getId())) continue;
      if (!chaves.add(a.getNome().toLowerCase())) continue;
      acoesUnicas.add(a);
    }
    dados.acoes = acoesUnicas;

    ids.clear();
    List<Recompensa> recompensasUnicas = new ArrayList<>();
    for (Recompensa r : dados.recompensas) {
      if (r.getId() == null || !ids.add(r.getId())) continue;
      recompensasUnicas.add(r);
    }
    dados.recompensas = recompensasUnicas;

    ids.clear();
    List<RegistroAcao> registrosUnicos = new ArrayList<>();
    for (RegistroAcao r : dados.registros) {
      if (r.getId() == null || !ids.add(r.getId())) continue;
      registrosUnicos.add(r);
    }
    dados.registros = registrosUnicos;

    dados.seqUsuario = maiorId(dados.usuarios.stream().map(Usuario::getId));
    dados.seqAcao = maiorId(dados.acoes.stream().map(AcaoSustentavel::getId));
    dados.seqRecompensa = maiorId(
      dados.recompensas.stream().map(Recompensa::getId)
    );
    dados.seqRegistro = maiorId(
      dados.registros.stream().map(RegistroAcao::getId)
    );
    persistir();
  }

  private static long maiorId(java.util.stream.Stream<Long> ids) {
    return ids.filter(Objects::nonNull).mapToLong(Long::longValue).max()
      .orElse(0);
  }

  private void semearAcoesPadrao() {
    String[][] padroes = {
      {"Reciclagem", "Separar resíduos recicláveis", "30"},
      {"Transporte", "Usar transporte público", "50"},
      {"Energia", "Economizar energia", "20"},
      {"Agua", "Economizar água", "20"},
      {"Bicicleta", "Pedalar em vez de usar carro", "40"},
      {"Plantio de arvore", "Plantar uma árvore", "100"},
      {"Banho rapido", "Banho de até 5 minutos", "20"},
    };
    boolean alterou = false;
    for (String[] p : padroes) {
      boolean existe = dados.acoes
        .stream()
        .anyMatch(a -> a.getNome().equalsIgnoreCase(p[0]));
      if (!existe) {
        AcaoSustentavel acao = new AcaoSustentavel(
          p[0],
          p[1],
          Integer.parseInt(p[2])
        );
        acao.setId(++dados.seqAcao);
        dados.acoes.add(acao);
        alterou = true;
      }
    }
    if (alterou) persistir();
  }

  public synchronized List<Usuario> listarUsuarios() {
    return new ArrayList<>(dados.usuarios);
  }

  public synchronized Usuario buscarUsuarioPorEmail(String email) {
    String alvo = email.trim().toLowerCase();
    return dados.usuarios
      .stream()
      .filter(u -> u.getEmail().equals(alvo))
      .findFirst()
      .orElse(null);
  }

  public synchronized Usuario buscarUsuarioPorNome(String nome) {
    String alvo = nome.trim().toLowerCase();
    return dados.usuarios
      .stream()
      .filter(u -> u.getNome().toLowerCase().equals(alvo))
      .findFirst()
      .orElse(null);
  }

  public synchronized Usuario buscarUsuarioPorId(long id) {
    return dados.usuarios
      .stream()
      .filter(u -> u.getId() != null && u.getId() == id)
      .findFirst()
      .orElse(null);
  }

  public synchronized void inserirUsuario(Usuario usuario) {
    Objects.requireNonNull(usuario, "Usuário é obrigatório");
    validarUsuarioUnico(usuario, -1L);
    usuario.setId(++dados.seqUsuario);
    dados.usuarios.add(usuario);
    persistir();
  }

  public synchronized void atualizarUsuario(Usuario usuario) {
    Objects.requireNonNull(usuario, "Usuário é obrigatório");
    validarUsuarioUnico(usuario, usuario.getId());
    substituir(dados.usuarios, usuario);
    persistir();
  }

  private void validarUsuarioUnico(Usuario usuario, Long idAtual) {
    for (Usuario u : dados.usuarios) {
      if (u.getId() != null && u.getId().equals(idAtual)) continue;
      if (u.getEmail().equals(usuario.getEmail())) {
        throw new IllegalArgumentException(
          "Já existe um usuário com este e-mail"
        );
      }
      if (u.getNome().equalsIgnoreCase(usuario.getNome())) {
        throw new IllegalStateException(
          "Este nome já está em uso por outro usuário"
        );
      }
    }
  }

  public synchronized void excluirUsuario(long id) {
    dados.usuarios.removeIf(u -> u.getId() != null && u.getId() == id);
    persistir();
  }

  public synchronized List<AcaoSustentavel> listarAcoes() {
    return new ArrayList<>(dados.acoes);
  }

  public synchronized AcaoSustentavel buscarAcaoPorId(long id) {
    return dados.acoes
      .stream()
      .filter(a -> a.getId() != null && a.getId() == id)
      .findFirst()
      .orElse(null);
  }

  public synchronized void inserirAcao(AcaoSustentavel acao) {
    Objects.requireNonNull(acao, "Ação é obrigatória");
    acao.setId(++dados.seqAcao);
    dados.acoes.add(acao);
    persistir();
  }

  public synchronized void atualizarAcao(AcaoSustentavel acao) {
    Objects.requireNonNull(acao, "Ação é obrigatória");
    substituir(dados.acoes, acao);
    persistir();
  }

  public synchronized void excluirAcao(long id) {
    dados.acoes.removeIf(a -> a.getId() != null && a.getId() == id);
    persistir();
  }

  public synchronized List<Recompensa> listarRecompensas() {
    return new ArrayList<>(dados.recompensas);
  }

  public synchronized Recompensa buscarRecompensaPorId(long id) {
    return dados.recompensas
      .stream()
      .filter(r -> r.getId() != null && r.getId() == id)
      .findFirst()
      .orElse(null);
  }

  public synchronized void inserirRecompensa(Recompensa recompensa) {
    Objects.requireNonNull(recompensa, "Recompensa é obrigatória");
    recompensa.setId(++dados.seqRecompensa);
    dados.recompensas.add(recompensa);
    persistir();
  }

  public synchronized void atualizarRecompensa(Recompensa recompensa) {
    Objects.requireNonNull(recompensa, "Recompensa é obrigatória");
    substituir(dados.recompensas, recompensa);
    persistir();
  }

  public synchronized void excluirRecompensa(long id) {
    dados.recompensas.removeIf(r -> r.getId() != null && r.getId() == id);
    persistir();
  }

  public synchronized List<RegistroAcao> listarRegistrosPorUsuario(
    long usuarioId
  ) {
    List<RegistroAcao> lista = new ArrayList<>();
    for (RegistroAcao r : dados.registros) {
      if (
        r.getUsuario() != null &&
        r.getUsuario().getId() != null &&
        r.getUsuario().getId() == usuarioId
      ) {
        lista.add(r);
      }
    }
    lista.sort(Comparator.comparing(RegistroAcao::getDataRegistro).reversed());
    return lista;
  }

  public synchronized void inserirRegistro(RegistroAcao registro) {
    Objects.requireNonNull(registro, "Registro é obrigatório");
    registro.setId(++dados.seqRegistro);
    dados.registros.add(registro);
    persistir();
  }

  private <T> void substituir(List<T> lista, T novo) {
    long id = extrairId(novo);
    for (int i = 0; i < lista.size(); i++) {
      if (extrairId(lista.get(i)) == id) {
        lista.set(i, novo);
        return;
      }
    }
    lista.add(novo);
  }

  private static long extrairId(Object objeto) {
    if (objeto instanceof Usuario u) return u.getId() == null
      ? Long.MIN_VALUE
      : u.getId();
    if (objeto instanceof AcaoSustentavel a) return a.getId() == null
      ? Long.MIN_VALUE
      : a.getId();
    if (objeto instanceof Recompensa r) return r.getId() == null
      ? Long.MIN_VALUE
      : r.getId();
    throw new IllegalArgumentException("Objeto sem identificador");
  }

  private void carregar() {
    try {
      if (Files.exists(ARQUIVO)) {
        Dados lido = gson.fromJson(
          Files.readString(ARQUIVO, StandardCharsets.UTF_8),
          Dados.class
        );
        if (lido != null) dados = lido;
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Falha ao ler " + ARQUIVO, e);
    }
  }

  private void persistir() {
    try {
      Files.createDirectories(ARQUIVO.getParent());
      Files.writeString(ARQUIVO, gson.toJson(dados), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("Falha ao salvar " + ARQUIVO, e);
    }
  }

  private static final class Dados {

    List<Usuario> usuarios = new ArrayList<>();
    List<AcaoSustentavel> acoes = new ArrayList<>();
    List<Recompensa> recompensas = new ArrayList<>();
    List<RegistroAcao> registros = new ArrayList<>();
    long seqUsuario;
    long seqAcao;
    long seqRecompensa;
    long seqRegistro;
  }

  static final class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
      out.value(
        value == null ? null : value.format(DateTimeFormatterHolder.FORMATO)
      );
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
      String valor = in.nextString();
      return valor == null ? null : LocalDateTime.parse(
        valor,
        DateTimeFormatterHolder.FORMATO
      );
    }
  }

  private static final class DateTimeFormatterHolder {

    static final java.time.format.DateTimeFormatter FORMATO =
      java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
  }
}

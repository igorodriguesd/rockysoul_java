package br.com.rockysoulup.model;

import java.text.Normalizer;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Cartinha colecionável com raridade e conjunto temático.
 * A lógica acompanha o conceito do frontend React: raridades mais raras têm
 * menor chance de aparecer e geram mais fragmentos ao repetirem.
 */
public class Carta {

    private static final Map<String, Integer> FRAGMENTOS_POR_RARIDADE = Map.of(
            "COMUM", 3,
            "INCOMUM", 5,
            "RARA", 10,
            "EPICA", 20,
            "LENDARIA", 40);

    private static final Map<String, Integer> CHANCE_DE_DROP = Map.of(
            "COMUM", 75,
            "INCOMUM", 55,
            "RARA", 35,
            "EPICA", 20,
            "LENDARIA", 10);

    private Long id;
    private String nome;
    private String descricao;
    private String conjunto;
    private String raridade;

    public Carta() {
    }

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
        if (id != null && id <= 0)
            throw new IllegalArgumentException(
                    "Id da carta inválido");
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException(
                    "O nome da carta é obrigatório");
        this.nome = nome.trim();
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao == null ? "" : descricao.trim();
    }

    public String getConjunto() {
        return conjunto;
    }

    public void setConjunto(String conjunto) {
        if (conjunto == null || conjunto.isBlank())
            throw new IllegalArgumentException(
                    "O conjunto da carta é obrigatório");
        this.conjunto = conjunto.trim();
    }

    public String getRaridade() {
        return raridade;
    }

    public void setRaridade(String raridade) {
        if (raridade == null || raridade.isBlank())
            throw new IllegalArgumentException(
                    "A raridade da carta é obrigatória");
        String normalizada = normalizarRaridade(raridade);
        if (!FRAGMENTOS_POR_RARIDADE.containsKey(normalizada)) {
            throw new IllegalArgumentException("Raridade inválida: " + raridade);
        }
        this.raridade = normalizada;
    }

    public int getFragmentosPorRaridade() {
        return FRAGMENTOS_POR_RARIDADE.get(raridade);
    }

    public int getChanceDeDrop() {
        return CHANCE_DE_DROP.get(raridade);
    }

    public static String sortearRaridadeAleatoria() {
        int valor = ThreadLocalRandom.current().nextInt(1, 101);
        return sortearRaridadeAleatoria(valor);
    }

    public static String sortearRaridadeAleatoria(int valorSorteado) {
        if (valorSorteado < 1 || valorSorteado > 100) {
            throw new IllegalArgumentException("O valor sorteado deve estar entre 1 e 100");
        }
        if (valorSorteado <= CHANCE_DE_DROP.get("COMUM")) {
            return "COMUM";
        }
        if (valorSorteado <= CHANCE_DE_DROP.get("COMUM") + CHANCE_DE_DROP.get("INCOMUM")) {
            return "INCOMUM";
        }
        if (valorSorteado <= CHANCE_DE_DROP.get("COMUM") + CHANCE_DE_DROP.get("INCOMUM") + CHANCE_DE_DROP.get("RARA")) {
            return "RARA";
        }
        if (valorSorteado <= CHANCE_DE_DROP.get("COMUM") + CHANCE_DE_DROP.get("INCOMUM") + CHANCE_DE_DROP.get("RARA")
                + CHANCE_DE_DROP.get("EPICA")) {
            return "EPICA";
        }
        return "LENDARIA";
    }

    public static String normalizarRaridade(String valor) {
        if (valor == null || valor.isBlank())
            return "";
        String semAcentos = Normalizer
                .normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcentos.toUpperCase().replace(" ", "_").replace("-", "_");
    }
}

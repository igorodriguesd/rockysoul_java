package br.com.rockysoulup.model;

import java.text.Normalizer;
import java.util.Map;

/**
 * Cartinha colecionável com raridade e conjunto temático.
 * A lógica acompanha o conceito do frontend React: raridades e fragmentos por
 * repetição.
 */
public class Carta {

    private static final Map<String, Integer> FRAGMENTOS_POR_RARIDADE = Map.of(
            "COMUM", 3,
            "INCOMUM", 5,
            "RARA", 10,
            "EPICA", 20,
            "LENDARIA", 40);

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

    public static String normalizarRaridade(String valor) {
        if (valor == null || valor.isBlank())
            return "";
        String semAcentos = Normalizer
                .normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcentos.toUpperCase().replace(" ", "_").replace("-", "_");
    }
}

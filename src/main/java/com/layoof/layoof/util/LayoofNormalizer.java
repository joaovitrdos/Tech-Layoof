package com.layoof.layoof.util;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Padronizacao dos campos que vem da IA.
 *
 * <p>O prompt pede padrao; esta classe garante. A diferenca importa: prompt e probabilistico e
 * degrada quando o modelo muda de versao, enquanto isto aqui e deterministico e testavel sem
 * rede. Toda instrucao de formato no prompt tem aqui a sua rede de seguranca.
 *
 * <p>Funcoes puras e estaticas, no mesmo espirito de {@link EmailNormalizer}: sem estado, sem
 * dependencia de Spring, testavel em milissegundos.
 */
public final class LayoofNormalizer {

    private static final int MAXIMO_DEMITIDOS_PLAUSIVEL = 500_000;

    private static final Pattern ESPACOS = Pattern.compile("\\s+");

    /** Sufixo societario polui a deduplicacao: "Google" e "Google LLC" viram dois registros. */
    private static final Pattern SUFIXO_SOCIETARIO = Pattern.compile(
            "(?i)[,\\s]+(inc|inc\\.|llc|l\\.l\\.c\\.|ltd|ltd\\.|ltda|ltda\\.|s\\.a\\.|sa|corp|corp\\.|"
                    + "corporation|co\\.|gmbh|plc|b\\.v\\.|nv|ag|pte|pty|group|holdings?)\\.?$");

    /** Sem esta tabela a mesma coluna guarda USA, U.S. e United States, e o filtro da home quebra. */
    private static final Map<String, String> PAISES = Map.ofEntries(
            Map.entry("usa", "Estados Unidos"),
            Map.entry("us", "Estados Unidos"),
            Map.entry("u.s.", "Estados Unidos"),
            Map.entry("u.s.a.", "Estados Unidos"),
            Map.entry("united states", "Estados Unidos"),
            Map.entry("united states of america", "Estados Unidos"),
            Map.entry("eua", "Estados Unidos"),
            Map.entry("brazil", "Brasil"),
            Map.entry("br", "Brasil"),
            Map.entry("india", "India"),
            Map.entry("uk", "Reino Unido"),
            Map.entry("united kingdom", "Reino Unido"),
            Map.entry("england", "Reino Unido"),
            Map.entry("germany", "Alemanha"),
            Map.entry("france", "Franca"),
            Map.entry("spain", "Espanha"),
            Map.entry("canada", "Canada"),
            Map.entry("mexico", "Mexico"),
            Map.entry("china", "China"),
            Map.entry("japan", "Japao"),
            Map.entry("israel", "Israel"),
            Map.entry("netherlands", "Holanda"),
            Map.entry("ireland", "Irlanda"),
            Map.entry("australia", "Australia"),
            Map.entry("singapore", "Singapura"),
            Map.entry("global", "Global"),
            Map.entry("worldwide", "Global"));

    private LayoofNormalizer() {
    }

    public static String empresa(String valor) {
        String limpo = texto(valor, 200);
        if (limpo == null) {
            return null;
        }
        String semSufixo = SUFIXO_SOCIETARIO.matcher(limpo).replaceAll("").strip();
        return semSufixo.isBlank() ? limpo : semSufixo;
    }

    public static String pais(String valor) {
        String limpo = texto(valor, 80);
        if (limpo == null) {
            return null;
        }
        return PAISES.getOrDefault(limpo.toLowerCase(Locale.ROOT), limpo);
    }

    /**
     * Numero implausivel e sinal de alucinacao ou de percentual convertido em absoluto.
     * Registro sem numero e melhor do que registro com numero inventado.
     */
    public static Integer quantidade(Integer valor) {
        if (valor == null || valor <= 0 || valor > MAXIMO_DEMITIDOS_PLAUSIVEL) {
            return null;
        }
        return valor;
    }

    public static String texto(String valor, int limite) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpo = ESPACOS.matcher(valor.strip()).replaceAll(" ");
        return limpo.length() <= limite ? limpo : limpo.substring(0, limite);
    }
}

package com.layoof.layoof.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class LayoofNormalizerTest {

    @Nested
    @DisplayName("empresa")
    class Empresa {

        @ParameterizedTest(name = "{0} -> {1}")
        @CsvSource({
                "Alphabet Inc.,        Alphabet",
                "'Meta Platforms, Inc.', Meta Platforms",
                "Amazon.com Inc,       Amazon.com",
                "Stone Pagamentos S.A., Stone Pagamentos",
                "SAP SE,               SAP SE",
                "Google,               Google"
        })
        @DisplayName("remove sufixo societario: sem isso Google e Google LLC viram dois registros")
        void removeSufixoSocietario(String entrada, String esperado) {
            assertThat(LayoofNormalizer.empresa(entrada)).isEqualTo(esperado);
        }

        @Test
        @DisplayName("colapsa espaco e nao devolve vazio disfarcado")
        void normalizaEspacos() {
            assertThat(LayoofNormalizer.empresa("  Nu   Pagamentos  ")).isEqualTo("Nu Pagamentos");
            assertThat(LayoofNormalizer.empresa("   ")).isNull();
            assertThat(LayoofNormalizer.empresa(null)).isNull();
        }
    }

    @Nested
    @DisplayName("pais")
    class Pais {

        @ParameterizedTest(name = "{0} -> Estados Unidos")
        @ValueSource(strings = {"USA", "usa", "U.S.", "United States", "united states of america", "EUA"})
        @DisplayName("converge as variantes: senao o filtro da home quebra")
        void convergeVariantesParaPortugues(String entrada) {
            assertThat(LayoofNormalizer.pais(entrada)).isEqualTo("Estados Unidos");
        }

        @Test
        @DisplayName("pais desconhecido passa como veio, sem inventar traducao")
        void mantemDesconhecido() {
            assertThat(LayoofNormalizer.pais("Estonia")).isEqualTo("Estonia");
            assertThat(LayoofNormalizer.pais(null)).isNull();
        }
    }

    @Nested
    @DisplayName("quantidade")
    class Quantidade {

        @Test
        @DisplayName("numero implausivel vira null: melhor sem numero do que com numero inventado")
        void recusaImplausivel() {
            assertThat(LayoofNormalizer.quantidade(0)).isNull();
            assertThat(LayoofNormalizer.quantidade(-5)).isNull();
            assertThat(LayoofNormalizer.quantidade(9_000_000)).isNull();
            assertThat(LayoofNormalizer.quantidade(null)).isNull();
        }

        @Test
        @DisplayName("numero plausivel passa intacto")
        void aceitaPlausivel() {
            assertThat(LayoofNormalizer.quantidade(14_000)).isEqualTo(14_000);
        }
    }

    @Test
    @DisplayName("texto e truncado no limite da coluna, e nao no insert")
    void truncaNoLimite() {
        String longo = "a".repeat(600);
        assertThat(LayoofNormalizer.texto(longo, 500)).hasSize(500);
        assertThat(LayoofNormalizer.texto("ok", 500)).isEqualTo("ok");
    }
}

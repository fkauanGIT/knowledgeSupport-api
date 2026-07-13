package com.knowledgeSupport.api.application.service;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testa normalize/tokenize/score isoladamente, sem precisar montar Called/Standard/mocks.
 * AnalyzeCalledServiceTest cobre a integração da cascata; aqui é só o algoritmo de texto.
 */
class TextSimilarityTest {

    @Test
    void normalize_removeAcentoCaixaEEspacosDuplicados() {
        assertEquals("nao fecha direito", TextSimilarity.normalize("  Não FECHA   direito "));
    }

    @Test
    void normalize_comStringNula_retornaVazio() {
        assertEquals("", TextSimilarity.normalize(null));
    }

    @Test
    void tokenize_removeStopwordsMasMantemNegacao() {
        Set<String> tokens = TextSimilarity.tokenize("O caixa não fecha de jeito nenhum!");

        assertTrue(tokens.contains("caixa"));
        assertTrue(tokens.contains("nao"), "negação não pode virar stopword");
        assertTrue(tokens.contains("fecha"));
        assertTrue(tokens.contains("jeito"));
        assertTrue(tokens.contains("nenhum"));
        assertFalse(tokens.contains("o"));
        assertFalse(tokens.contains("de"));
    }

    @Test
    void score_textosIdenticosIgnorandoAcentoECaixa_retornaUm() {
        double score = TextSimilarity.score("Caixa não fecha", "caixa NAO fecha");

        assertEquals(1.0, score);
    }

    @Test
    void score_textosSemPalavraEmComum_retornaZero() {
        double score = TextSimilarity.score("Relatorio de vendas trava", "Estoque duplicado no cadastro");

        assertEquals(0.0, score);
    }

    @Test
    void score_textoVazio_retornaZero() {
        assertEquals(0.0, TextSimilarity.score("", "qualquer coisa"));
        assertEquals(0.0, TextSimilarity.score(null, "qualquer coisa"));
    }

    @Test
    void score_toleraTypoDeUmaLetra() {
        // "fechamneto" tem duas letras trocadas de lugar em relação a "fechamento"
        double score = TextSimilarity.score("erro de fechamento duplicado", "erro de fechamneto duplicado");

        assertTrue(score > 0.8, "esperava score alto mesmo com o typo, veio " + score);
    }

    @Test
    void score_naoConfundePalavrasCurtasDiferentes() {
        // "sim" e "nao" tem o mesmo tamanho mas significados opostos: nao podem virar typo uma da outra.
        // Se confundisse, score seria 4/4=1.0 em vez de 3/4.
        double score = TextSimilarity.score("usuario respondeu sim no formulario", "usuario respondeu nao no formulario");

        assertEquals(0.75, score);
    }

    @Test
    void score_chamadoComPoucosTokens_retornaZeroMesmoBatendoTudo() {
        // Guarda contra containment inflado: 1 ou 2 tokens batendo por acaso nao deveriam
        // "cobrir" um Standard inteiro com confianca de 100%.
        double score = TextSimilarity.score("erro", "ocorre um erro generico no sistema inteiro");

        assertEquals(0.0, score);
    }

    @Test
    void score_ehAssimetrico_chamadoCurtoDentroDeStandardLongoScoraAlto() {
        String textoChamado = "cliente de outro estado nao consegue incluir item no pedido";
        String textoStandardRico = "rotina 4116 pode falhar de duas formas: sem preco ao buscar produto, "
                + "ou cliente de outro estado nao consegue incluir item no pedido por erro de tributacao";

        double score = TextSimilarity.score(textoChamado, textoStandardRico);

        assertTrue(score >= 0.4, "chamado curto contido num Standard rico deveria pontuar alto, veio " + score);
    }
}

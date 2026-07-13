package com.knowledgeSupport.api.application.service;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Comparação textual pura (sem Spring, sem I/O): normaliza, tokeniza e mede quanto do
 * texto do chamado está coberto pelo texto do Standard, tolerando erro de digitação.
 * Usada pelo AnalyzeCalledService para pontuar Called x Standard quando a igualdade
 * exata de errorName não resolve.
 */
public final class TextSimilarity {

    // Lista fixa e pequena de propósito: só as palavras mais comuns do PT que não carregam
    // significado de negócio. Cresce sob demanda, não precisa ser exaustiva.
    // "nao" fica de fora de propósito: é negação, não ruído — remover mudaria o sentido
    // ("caixa fecha" x "caixa nao fecha" são problemas opostos, não sinônimos).
    private static final Set<String> STOPWORDS_PT = Set.of(
            "a", "o", "as", "os", "de", "do", "da", "dos", "das", "em", "no", "na", "nos", "nas",
            "um", "uma", "uns", "umas", "e", "ou", "que", "com", "sem", "para", "por", "pra",
            "ao", "aos", "à", "às", "se", "sua", "seu", "suas", "seus", "ja", "mas", "como",
            "quando", "depois", "antes", "esta", "este", "esse", "essa", "isso", "foi", "ser",
            "tem", "ha", "pelo", "pela", "num", "numa", "ate"
    );

    private static final LevenshteinDistance LEVENSHTEIN = LevenshteinDistance.getDefaultInstance();

    // Chamado com poucos tokens não tem informação suficiente pra sustentar um containment
    // score confiável (ex: 1 token batendo por acaso não deveria "cobrir" um Standard inteiro).
    private static final int MIN_TOKENS_CHAMADO = 3;

    private TextSimilarity() {}

    public static String normalize(String value) {
        if (value == null) return "";
        String semAcentos = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

    public static Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) return Set.of();
        return Arrays.stream(normalize(text).split("[^\\p{L}\\p{N}]+"))
                .filter(token -> !token.isBlank())
                .filter(token -> !STOPWORDS_PT.contains(token))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    /**
     * Containment score, não Jaccard simétrico: mede quanto do CHAMADO está coberto pelo
     * texto do Standard (interseção / tokens do chamado), com tolerância a typo. Assimétrico
     * de propósito — o chamado tende a ser curto e o Standard cresce com o tempo (acumula
     * variações de sintoma); dividir pela união penalizaria Standards ricos, que é exatamente
     * o comportamento que queremos incentivar. Resultado entre 0 e 1; textoChamado precisa
     * vir primeiro, a ordem dos argumentos importa.
     */
    public static double score(String textoChamado, String textoStandard) {
        Set<String> tokensChamado = tokenize(textoChamado);
        Set<String> tokensStandard = tokenize(textoStandard);

        if (tokensChamado.size() < MIN_TOKENS_CHAMADO || tokensStandard.isEmpty()) return 0.0;

        Set<String> disponiveisStandard = new HashSet<>(tokensStandard);
        int matches = 0;
        for (String tokenChamado : tokensChamado) {
            String encontrado = null;
            for (String tokenStandard : disponiveisStandard) {
                if (isFuzzyMatch(tokenChamado, tokenStandard)) {
                    encontrado = tokenStandard;
                    break;
                }
            }
            if (encontrado != null) {
                disponiveisStandard.remove(encontrado);
                matches++;
            }
        }

        return (double) matches / tokensChamado.size();
    }

    private static boolean isFuzzyMatch(String a, String b) {
        if (a.equals(b)) return true;
        int menor = Math.min(a.length(), b.length());
        if (menor <= 2) return false; // token curto demais: só aceita igualdade exata
        int maior = Math.max(a.length(), b.length());
        int tolerancia = maior <= 4 ? 1 : 2;
        return LEVENSHTEIN.apply(a, b) <= tolerancia;
    }
}

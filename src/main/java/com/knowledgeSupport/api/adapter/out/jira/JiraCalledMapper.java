package com.knowledgeSupport.api.adapter.out.jira;

import com.knowledgeSupport.api.domain.model.Called;
import com.knowledgeSupport.api.domain.model.Requester;
import com.knowledgeSupport.api.domain.model.enums.FilterCategory;
import com.knowledgeSupport.api.domain.model.enums.IncidentType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Tradutor da fronteira Jira -> domínio.
 * O "idioma" do Jira (summary, reporter, ADF...) morre aqui dentro:
 * o resto do sistema só enxerga Called e Requester.
 * Mesmo papel do StandardMapper, só que para outra fronteira.
 */
public final class JiraCalledMapper {

    private static final String JIRA_DATETIME = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final String JIRA_DATE = "yyyy-MM-dd";

    // Respostas de rejeição (ex: SEFAZ) vêm com timestamp de log colado na frente
    // ("11/07/2026 11:29:52 - Resposta da Sefaz: 866 - ..."). Isso é ruído de integração,
    // não parte do erro em si — sobrando aí, quebra o match exato porque nunca se repete
    // igual de um chamado pro outro.
    private static final Pattern TIMESTAMP_PREFIX = Pattern.compile("(?m)^\\d{2}/\\d{2}/\\d{4}\\s+\\d{2}:\\d{2}:\\d{2}\\s*-\\s*");

    private JiraCalledMapper() {}

    public static Called toDomain(JiraIssuePayload issue) {
        JiraFields fields = issue.fields();

        Requester requester = fields.reporter() == null ? null : new Requester(
                fields.reporter().displayName(),
                null, // o Jira não conhece a filial; decidir depois de onde ela vem
                fields.reporter().emailAddress()
        );

        return new Called(
                fields.summary(),                       // titleCalled
                extractText(fields.description()),      // descriptionCalled
                stripTimestampPrefix(extractText(fields.errorName())), // errorName (customfield_10433)
                IncidentType.ERROR,                     // TODO derivar do tipo/labels da issue
                FilterCategory.PENDING,                 // TODO derivar do status/projeto
                requester,
                parse(fields.created(), JIRA_DATETIME), // createdAt
                parse(fields.duedate(), JIRA_DATE),     // deadline
                parse(fields.updated(), JIRA_DATETIME), // updateAt
                fields.routineNumber() == null ? null : fields.routineNumber().intValue() // routineNumber (customfield_10432)
        );
    }

    static String stripTimestampPrefix(String value) {
        if (value == null) return null;
        String semTimestamp = TIMESTAMP_PREFIX.matcher(value).replaceAll("").trim();
        return semTimestamp.isEmpty() ? null : semTimestamp;
    }

    /**
     * Percorre a árvore ADF recursivamente e concatena os nós de texto.
     */
    static String extractText(JiraDoc node) {
        if (node == null) return null;
        StringBuilder sb = new StringBuilder();
        collectText(node, sb);
        String result = sb.toString().trim();
        return result.isEmpty() ? null : result;
    }

    private static void collectText(JiraDoc node, StringBuilder sb) {
        if (node.text() != null) {
            sb.append(node.text());
        }
        if (node.content() != null) {
            for (JiraDoc child : node.content()) {
                collectText(child, sb);
            }
            if ("paragraph".equals(node.type())) {
                sb.append('\n');
            }
        }
    }

    private static Date parse(String value, String pattern) {
        if (value == null || value.isBlank()) return null;
        try {
            return new SimpleDateFormat(pattern).parse(value);
        } catch (ParseException e) {
            return null;
        }
    }
}

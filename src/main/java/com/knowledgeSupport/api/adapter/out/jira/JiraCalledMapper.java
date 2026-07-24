package com.knowledgeSupport.api.adapter.out.jira;

import com.knowledgeSupport.api.domain.model.Called;
import com.knowledgeSupport.api.domain.model.Requester;
import com.knowledgeSupport.api.domain.model.enums.FilterCategory;
import com.knowledgeSupport.api.domain.model.enums.IncidentType;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Translator for the Jira boundary -> domain.
 * Jira's "language" (summary, reporter, ADF...) dies right here:
 * the rest of the system only ever sees Called and Requester.
 * Same role as StandardMapper, just for a different boundary.
 */
public final class JiraCalledMapper {

    // DateTimeFormatter é imutável/thread-safe e reutilizável — SimpleDateFormat era instanciado
    // por campo, por issue (3x N objetos numa listagem grande) e não é thread-safe.
    private static final DateTimeFormatter JIRA_DATETIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ROOT);

    // Rejection responses (e.g. SEFAZ) come with a log timestamp glued to the front
    // ("11/07/2026 11:29:52 - Sefaz response: 866 - ..."). That's integration noise,
    // not part of the error itself — left in, it breaks the exact match because it
    // never repeats identically from one ticket to another.
    private static final Pattern TIMESTAMP_PREFIX = Pattern.compile("(?m)^\\d{2}/\\d{2}/\\d{4}\\s+\\d{2}:\\d{2}:\\d{2}\\s*-\\s*");

    private JiraCalledMapper() {}

    public static Called toDomain(JiraIssuePayload issue) {
        JiraFields fields = issue.fields();

        Requester requester = fields.reporter() == null ? null : new Requester(
                fields.reporter().displayName(),
                fields.reporter().emailAddress()
        );

        return Called.builder()
                .jiraKey(issue.key())
                .titleCalled(fields.summary())
                .descriptionCalled(extractText(fields.description()))
                .errorName(stripTimestampPrefix(extractText(fields.errorName()))) // customfield_10433
                .incidentType(incidentTypeFrom(fields.issuetype()))
                // FilterCategory (SUPPORT/INFRASTRUCTURE/DEVELOPMENT) has no reliable signal
                // in the fields Jira returns today (status is workflow progress, not a work
                // category). Every ticket that reaches this system today is support, so it
                // defaults to SUPPORT until a real field exists to tell the categories apart
                // — see docs/LIMITATIONS.md.
                .filterCategory(FilterCategory.SUPPORT)
                .status(fields.status() == null ? null : fields.status().name())
                .requester(requester)
                .assigneeName(fields.assignee() == null ? null : fields.assignee().displayName())
                .createdAt(parseDateTime(fields.created()))
                .deadline(parseDate(fields.duedate()))
                .updateAt(parseDateTime(fields.updated()))
                .routineNumber(fields.routineNumber() == null ? null : fields.routineNumber().intValue()) // customfield_10432
                .build();
    }

    private static IncidentType incidentTypeFrom(JiraIssueType issuetype) {
        if (issuetype == null || issuetype.name() == null) return IncidentType.ERROR;
        return issuetype.name().toLowerCase(java.util.Locale.ROOT).contains("alerta") ? IncidentType.ALERT : IncidentType.ERROR;
    }

    static String stripTimestampPrefix(String value) {
        if (value == null) return null;
        String withoutTimestamp = TIMESTAMP_PREFIX.matcher(value).replaceAll("").trim();
        return withoutTimestamp.isEmpty() ? null : withoutTimestamp;
    }

    /**
     * Recursively walks the ADF tree and concatenates the text nodes.
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

    private static Date parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Date.from(OffsetDateTime.parse(value, JIRA_DATETIME).toInstant());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static Date parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Date.from(LocalDate.parse(value).atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}

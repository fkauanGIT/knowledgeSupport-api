package com.knowledgeSupport.api.adapter.out.jira;

/**
 * Dry-runs a candidate set of Jira credentials/JQL before they get persisted, so a typo in
 * the JQL or a wrong baseUrl fails the PUT immediately instead of surfacing later as a
 * broken /api/calleds.
 */
public interface JiraCredentialsValidator {
    /**
     * @throws JiraCredentialsInvalidException if Jira rejects the candidate (bad JQL,
     *                                          wrong baseUrl, invalid email/token, ...)
     */
    void validate(JiraCredentials candidate);
}

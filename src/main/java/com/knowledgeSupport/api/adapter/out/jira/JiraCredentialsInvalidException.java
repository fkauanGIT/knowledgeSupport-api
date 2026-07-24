package com.knowledgeSupport.api.adapter.out.jira;

/** Thrown by {@link JiraCredentialsValidator} when Jira itself rejects the candidate config. */
public class JiraCredentialsInvalidException extends RuntimeException {
    public JiraCredentialsInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}

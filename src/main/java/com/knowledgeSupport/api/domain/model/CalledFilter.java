package com.knowledgeSupport.api.domain.model;

import java.time.LocalDate;

/**
 * Optional narrowing layered on top of whatever JQL is already configured
 * (JIRA_JQL / PUT /api/settings/jira). An all-null filter keeps today's behavior:
 * everything the configured JQL returns, unfiltered — dashboards/monitoring can then
 * opt into narrower slices (a date window, only-open) without changing that default.
 */
public record CalledFilter(LocalDate createdFrom, LocalDate createdTo, Boolean onlyOpen) {

    public static final CalledFilter NONE = new CalledFilter(null, null, null);
}

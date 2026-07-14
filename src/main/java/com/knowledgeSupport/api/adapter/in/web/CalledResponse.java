package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.Called;
import com.knowledgeSupport.api.domain.model.enums.FilterCategory;
import com.knowledgeSupport.api.domain.model.enums.IncidentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

/**
 * Shape of the JSON OUR API returns (web boundary, response side).
 * Same role as StandardResponse.
 */
@Schema(description = "Support ticket, translated from Jira into the domain format")
public record CalledResponse(

        @Schema(description = "Ticket key in Jira — use it to build the /api/calleds/{key}/analysis URL",
                example = "SUP-1123")
        String jiraKey,

        @Schema(description = "Ticket title (summary in Jira)",
                example = "Buyer permission won't let them log into the HUB")
        String titleCalled,

        @Schema(description = "Plain-text description (extracted from Jira's ADF)",
                example = "Users with a buyer profile get an error when logging in...")
        String descriptionCalled,

        @Schema(description = "Routine number")
        Integer routineNumber,

        @Schema(description = "Error name used to compare against the patterns (today mirrors the title)")
        String errorName,

        @Schema(description = "Incident type", example = "ERROR")
        IncidentType incidentType,

        @Schema(description = "Triage category", example = "PENDING")
        FilterCategory filterCategory,

        @Schema(description = "Current status in Jira", example = "Waiting for customer")
        String status,

        @Schema(description = "Name of whoever opened the ticket (reporter in Jira)", example = "Francisco Kauan")
        String requesterName,

        @Schema(description = "Creation date in Jira")
        Date createdAt,

        @Schema(description = "Deadline (duedate in Jira), if set", nullable = true)
        Date deadline,

        @Schema(description = "Last update in Jira")
        Date updateAt) {

    public static CalledResponse from(Called called) {
        return new CalledResponse(
                called.getJiraKey(),
                called.getTitleCalled(),
                called.getDescriptionCalled(),
                called.getRoutineNumber(),
                called.getErrorName(),
                called.getIncidentType(),
                called.getFilterCategory(),
                called.getStatus(),
                called.getRequester() == null ? null : called.getRequester().getRequesterName(),
                called.getCreatedAt(),
                called.getDeadline(),
                called.getUpdateAt()
        );
    }
}

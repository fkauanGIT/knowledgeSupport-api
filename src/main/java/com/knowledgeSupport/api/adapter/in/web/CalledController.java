package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.application.port.in.AnalyzeCalledUseCase;
import com.knowledgeSupport.api.application.port.in.GapReportUseCase;
import com.knowledgeSupport.api.application.port.in.ListCalledsUseCase;
import com.knowledgeSupport.api.application.port.in.SubmitFeedbackUseCase;
import com.knowledgeSupport.api.domain.model.CalledFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Inbound adapter (web channel). Deliberately has no POST/PUT/DELETE:
 * tickets are born in Jira, not in our API. Here we only query.
 */
@RestController
@RequestMapping("/api/calleds")
@Tag(name = "Calleds", description = "Support tickets queried live from Jira — read-only. The filter is configurable via JIRA_JQL in .env.")
public class CalledController {

    private final ListCalledsUseCase listCalledsUseCase;
    private final AnalyzeCalledUseCase analyzeCalledUseCase;
    private final GapReportUseCase gapReportUseCase;
    private final SubmitFeedbackUseCase submitFeedbackUseCase;

    public CalledController(ListCalledsUseCase listCalledsUseCase, AnalyzeCalledUseCase analyzeCalledUseCase,
                            GapReportUseCase gapReportUseCase, SubmitFeedbackUseCase submitFeedbackUseCase) {
        this.listCalledsUseCase = listCalledsUseCase;
        this.analyzeCalledUseCase = analyzeCalledUseCase;
        this.gapReportUseCase = gapReportUseCase;
        this.submitFeedbackUseCase = submitFeedbackUseCase;
    }

    @GetMapping
    @Operation(summary = "Lists tickets, optionally filtered",
            description = "Queries the Jira API (project SUP) in real time and returns the tickets already "
                    + "translated to the domain format — no cache and no local persistence: Jira is the source "
                    + "of truth. With no query params, returns everything the configured JQL (JIRA_JQL / "
                    + "PUT /api/settings/jira) already returns. The params below only narrow that down; they "
                    + "don't replace the base filter — handy for monitoring/dashboards over a specific slice.")
    @ApiResponse(responseCode = "200", description = "List of tickets (may be empty)")
    public List<CalledResponse> listOpen(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Only tickets created on/after this date", example = "2026-01-01")
            LocalDate createdFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Only tickets created on/before this date (inclusive)", example = "2026-07-31")
            LocalDate createdTo,
            @RequestParam(required = false)
            @Parameter(description = "true = only tickets not in a Done-like status; "
                    + "false = only Done-like; omitted = no status filter")
            Boolean onlyOpen) {
        CalledFilter filter = new CalledFilter(createdFrom, createdTo, onlyOpen);
        return listCalledsUseCase.listOpenCalleds(filter).stream()
                .map(CalledResponse::from)
                .toList();
    }

    @GetMapping("/{key}/analysis")
    @Operation(summary = "Analyzes a ticket and tries to find a matching Standard",
            description = "Fetches the ticket from Jira by key and compares it against the registered Standards by routine.")
    @ApiResponse(responseCode = "200", description = "Analysis result")
    public CalledAnalysisResponse analyze(@PathVariable String key) {
        return CalledAnalysisResponse.from(analyzeCalledUseCase.analyze(key));
    }

    @GetMapping("/gap-report")
    @Operation(summary = "Gap report: where registering a new Standard yields the most coverage",
            description = "Runs the analysis over every open ticket and groups by routine the ones that found no Standard (method NONE), sorted from highest volume to lowest.")
    @ApiResponse(responseCode = "200", description = "Gap report")
    public GapReportResponse gapReport() {
        return GapReportResponse.from(gapReportUseCase.generate());
    }

    @PostMapping("/{key}/feedback")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Records whether the analysis's suggestion solved the ticket",
            description = "Feeds the Standard's accuracy rate (GET /api/standards/{id}/accuracy) with a real outcome, not a guess.")
    @ApiResponse(responseCode = "201", description = "Feedback recorded")
    public FeedbackResponse submitFeedback(@PathVariable String key, @Valid @RequestBody FeedbackRequest request) {
        return FeedbackResponse.from(submitFeedbackUseCase.submit(key, request.standardId(), request.resolved()));
    }
}

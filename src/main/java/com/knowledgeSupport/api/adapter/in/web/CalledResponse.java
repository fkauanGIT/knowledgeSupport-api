package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.Called;
import com.knowledgeSupport.api.domain.model.enums.FilterCategory;
import com.knowledgeSupport.api.domain.model.enums.IncidentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

/**
 * Formato do JSON que a NOSSA API devolve (fronteira web, lado de saída da resposta).
 * Mesmo papel do StandardResponse.
 */
@Schema(description = "Chamado de suporte, traduzido do Jira para o formato do domínio")
public record CalledResponse(

        @Schema(description = "Key do chamado no Jira — use pra montar a URL de /api/calleds/{key}/analysis",
                example = "SUP-1123")
        String jiraKey,

        @Schema(description = "Título do chamado (summary no Jira)",
                example = "Permissão de comprador não deixa fazer login no HUB")
        String titleCalled,

        @Schema(description = "Descrição em texto puro (extraída do ADF do Jira)",
                example = "Usuários com perfil de comprador recebem erro ao logar...")
        String descriptionCalled,

        @Schema(description = "Número da rotina")
        Integer routineNumber,

        @Schema(description = "Nome do erro para comparação com os padrões (hoje espelha o título)")
        String errorName,

        @Schema(description = "Tipo do incidente", example = "ERROR")
        IncidentType incidentType,

        @Schema(description = "Categoria de triagem", example = "PENDING")
        FilterCategory filterCategory,

        @Schema(description = "Status atual no Jira", example = "Aguardando cliente")
        String status,

        @Schema(description = "Nome de quem abriu o chamado (reporter no Jira)", example = "Francisco Kauan")
        String requesterName,

        @Schema(description = "Data de criação no Jira")
        Date createdAt,

        @Schema(description = "Prazo (duedate no Jira), se definido", nullable = true)
        Date deadline,

        @Schema(description = "Última atualização no Jira")
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

package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.Standard;
import com.knowledgeSupport.api.domain.model.enums.IncidentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Padrão de erro cadastrado na base de conhecimento")
public record StandardResponse(

        @Schema(description = "Identificador único gerado pelo sistema",
                example = "3f2c9a1e-8b4d-4e2a-9c7f-1a2b3c4d5e6f")
        UUID id,

        @Schema(description = "Nome curto que identifica o padrão", example = "Permissao de login HUB")
        String standardName,

        @Schema(description = "Descrição do erro/sintoma", example = "Usuario com perfil comprador nao consegue logar no HUB")
        String text,

        @Schema(description = "Solução padrão", example = "Adicionar o perfil COMPRADOR na tela de acessos do usuario")
        String result,

        @Schema(description = "Tipo do incidente", example = "ERROR")
        IncidentType incidentType) {

    public static StandardResponse from(Standard standard) {
        return new StandardResponse(standard.getId(), standard.getStandardName(), standard.getText(), standard.getResult(), standard.getIncidentType());
    }
}

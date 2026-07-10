package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.domain.model.enums.IncidentType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para cadastrar ou atualizar um padrão de erro")
public record StandardRequest(

        @Schema(description = "Nome curto que identifica o padrão", example = "Permissao de login HUB")
        String standardName,

        @Schema(description = "Descrição do erro/sintoma como ele aparece nos chamados",
                example = "Usuario com perfil comprador nao consegue logar no HUB")
        String text,

        @Schema(description = "Solução padrão: o passo a passo que resolve o erro",
                example = "Adicionar o perfil COMPRADOR na tela de acessos do usuario")
        String result,

        @Schema(description = "Tipo do incidente", example = "ERROR")
        IncidentType incidentType) {
}

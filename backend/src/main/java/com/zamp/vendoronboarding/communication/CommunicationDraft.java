package com.zamp.vendoronboarding.communication;

import com.zamp.vendoronboarding.entity.enums.CommunicationType;
import com.zamp.vendoronboarding.entity.enums.GenerationMethod;

public record CommunicationDraft(
        CommunicationType communicationType,
        String subject,
        String body,
        GenerationMethod generationMethod,
        String rawLlmResponse
) {
    public static CommunicationDraft fromLlm(CommunicationType communicationType,
                                             String subject,
                                             String body,
                                             String rawLlmResponse) {
        return new CommunicationDraft(communicationType, subject, body, GenerationMethod.LLM, rawLlmResponse);
    }

    public static CommunicationDraft fromFallback(CommunicationType communicationType,
                                                  String subject,
                                                  String body,
                                                  String errorMessage) {
        return new CommunicationDraft(communicationType, subject, body, GenerationMethod.FALLBACK, errorMessage);
    }
}

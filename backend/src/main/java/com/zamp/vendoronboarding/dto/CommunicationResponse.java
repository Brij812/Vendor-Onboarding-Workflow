package com.zamp.vendoronboarding.dto;

import com.zamp.vendoronboarding.entity.enums.CommunicationType;
import com.zamp.vendoronboarding.entity.enums.GenerationMethod;

public record CommunicationResponse(
        CommunicationType communicationType,
        String subject,
        String body,
        GenerationMethod generationMethod
) {
}

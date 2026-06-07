package com.zamp.vendoronboarding.ai;

public interface LlmClient {

    LlmCompletionResult complete(LlmRequest request);
}

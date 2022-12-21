package com.saveourtool.save.test.generator

enum class RequestedAnalysisMode(val requiresArgument: Boolean = false) {
    /**
     * A synonym for `--failure 0%`.
     */
    PERMANENT_SUCCESS,

    /**
     * A synonym for `--failure 100%`.
     */
    PERMANENT_FAILURE,

    FAILURE(requiresArgument = true),

    REGRESSION(requiresArgument = true),
    ;
}

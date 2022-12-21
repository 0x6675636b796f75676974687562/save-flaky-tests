package com.saveourtool.save.test.generator

import com.saveourtool.save.test.generator.RequestedAnalysisMode.PERMANENT_FAILURE

/**
 * A synonym for `--failure 100%`.
 */
object PermanentFailure : RequestedAnalysisResult {
    override val mode = PERMANENT_FAILURE
}

package com.saveourtool.save.test.generator

import com.saveourtool.save.test.generator.RequestedAnalysisMode.PERMANENT_SUCCESS

/**
 * A synonym for `--failure 0%`.
 */
object PermanentSuccess : RequestedAnalysisResult {
    override val mode = PERMANENT_SUCCESS
}

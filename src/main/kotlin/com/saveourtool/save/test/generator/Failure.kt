package com.saveourtool.save.test.generator

import com.saveourtool.save.test.generator.RequestedAnalysisMode.FAILURE

data class Failure(val ratePercentage: Int) : RequestedAnalysisResult {
    init {
        require(ratePercentage in 0..100)
    }

    override val mode = FAILURE
}

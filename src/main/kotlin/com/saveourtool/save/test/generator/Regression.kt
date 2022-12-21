package com.saveourtool.save.test.generator

import com.saveourtool.save.test.generator.RequestedAnalysisMode.REGRESSION

data class Regression(val regressionMode: RegressionMode) : RequestedAnalysisResult {
    override val mode = REGRESSION
}

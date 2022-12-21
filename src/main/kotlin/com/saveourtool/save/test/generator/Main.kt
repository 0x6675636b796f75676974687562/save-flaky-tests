package com.saveourtool.save.test.generator

import com.saveourtool.save.test.generator.RegressionMode.FAIL_ON_EVEN_DAYS
import com.saveourtool.save.test.generator.RegressionMode.FAIL_ON_ODD_DAYS
import com.saveourtool.save.test.generator.RequestedAnalysisMode.FAILURE
import com.saveourtool.save.test.generator.RequestedAnalysisMode.PERMANENT_FAILURE
import com.saveourtool.save.test.generator.RequestedAnalysisMode.PERMANENT_SUCCESS
import com.saveourtool.save.test.generator.RequestedAnalysisMode.REGRESSION
import java.nio.file.Path
import java.time.LocalDate
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.isRegularFile
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.system.exitProcess

object Main {
    private const val DEFAULT_ERROR_MESSAGE = "Error message"

    private const val LONG_OPTION_PREFIX = "--"

    private const val ARGUMENT_SEPARATOR = "--"

    private const val PERCENTAGE_SUFFIX = "%"

    @JvmStatic
    fun main(vararg args: String) {
        if (args.isEmpty()) {
            fatalError("The list of arguments is empty")
        }

        val (result, file) = parseArguments(args.toList())
        println("Java version: ${System.getProperty("java.version")}")
        println("Analyzing ${file.absolutePathString()}...")
        when (result) {
            is PermanentSuccess -> defaultSuccessAction(file)

            is PermanentFailure -> printLinterError(file, "Permanent failure")

            is Regression -> {
                val dayOfMonth = LocalDate.now().dayOfMonth
                val fail = when (result.regressionMode) {
                    FAIL_ON_ODD_DAYS -> dayOfMonth.isOdd
                    FAIL_ON_EVEN_DAYS -> dayOfMonth.isEven
                }
                when {
                    fail -> printLinterError(
                        file,
                        "Regression, mode = ${result.regressionMode}"
                    )

                    else -> defaultSuccessAction(file)
                }
            }

            is Failure -> {
                val fail = Random.nextInt(0..99) < result.ratePercentage

                when {
                    fail -> printLinterError(
                        file,
                        "Failure with ${result.ratePercentage}% rate"
                    )

                    else -> defaultSuccessAction(file)
                }
            }
        }
        println("Done.")
    }

    private fun parseArguments(args: List<String>): Pair<RequestedAnalysisResult, Path> {
        require(args.isNotEmpty())

        val it = args.iterator()

        val mode = if (it.hasNext()) {
            it.next().toAnalysisMode()
        } else {
            fatalError("Can't determine the mode from args: $args")
        }

        val result = if (mode.requiresArgument) {
            if (it.hasNext()) {
                mode.parseArgument(it.next())
            } else {
                fatalError("Mode $mode requires an argument")
            }
        } else {
            mode.toResult()
        }

        val file = it.getFile()

        val extraArgs = sequence {
            while (it.hasNext()) {
                yield(it.next())
            }
        }.toList()

        if (extraArgs.isNotEmpty()) {
            fatalError("Unexpected arguments: $extraArgs")
        }

        if (!file.isRegularFile()) {
            fatalError("Not a regular file or file doesn't exist: $file")
        }

        return result to file
    }

    private fun String.toAnalysisMode(): RequestedAnalysisMode =
        enumValues<RequestedAnalysisMode>().asSequence().firstOrNull {
            this == LONG_OPTION_PREFIX + it.toCommandLineArgument()
        } ?: fatalError("No analysis mode named \"$this\" found")

    private fun String.toRegressionMode(): RegressionMode =
        enumValues<RegressionMode>().asSequence().firstOrNull {
            this == it.toCommandLineArgument()
        } ?: fatalError("No regression mode named \"$this\" found")

    private fun RequestedAnalysisMode.parseArgument(arg: String): RequestedAnalysisResult {
        require(requiresArgument)

        if (arg == ARGUMENT_SEPARATOR) {
            fatalError("Mode $this requires an argument")
        }

        return when (this) {
            REGRESSION -> Regression(arg.toRegressionMode())

            FAILURE -> when {
                arg.endsWith(PERCENTAGE_SUFFIX) -> {
                    val rawPercentage = arg.substring(0, arg.length - PERCENTAGE_SUFFIX.length)
                    val percentage = rawPercentage.toIntOrNull()
                        ?: fatalError("Unparseable failure rate percentage: $arg")
                    when (percentage) {
                        in 0..100 -> Failure(ratePercentage = percentage)
                        else -> fatalError("Failure rate percentage should be in range 0..100: $percentage")
                    }
                }

                else -> fatalError("Unparseable failure rate percentage: $arg")
            }

            else -> fatalError("Mode $this is not supported")
        }
    }

    private fun RequestedAnalysisMode.toResult(): RequestedAnalysisResult {
        require(!requiresArgument)

        return when (this) {
            PERMANENT_SUCCESS -> PermanentSuccess
            PERMANENT_FAILURE -> PermanentFailure
            else -> fatalError("Mode $this is not supported")
        }
    }

    private fun Iterator<String>.getFile(): Path {
        while (hasNext()) {
            val file = next()

            if (file == ARGUMENT_SEPARATOR) {
                continue
            }

            return Path(file)
        }

        fatalError("No file specified")
    }

    private fun defaultSuccessAction(@Suppress("UNUSED_PARAMETER") file: Path) = Unit

    private fun printLinterError(
        file: Path,
        message: String = DEFAULT_ERROR_MESSAGE,
    ) {
        println("${file.absolutePathString()}:1:1: $message")
    }

    private fun fatalError(message: String): Nothing {
        println("ERROR: $message")
        exitProcess(1)
    }

    private val Int.isEven: Boolean
        get() =
            this % 2 == 0

    private val Int.isOdd: Boolean
        get() =
            !isEven

    private fun <T : Enum<T>> T.toCommandLineArgument(): String =
        name.lowercase().replace('_', '-')
}

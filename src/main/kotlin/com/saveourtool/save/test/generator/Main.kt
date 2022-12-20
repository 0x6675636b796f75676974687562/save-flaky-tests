package com.saveourtool.save.test.generator

import kotlin.system.exitProcess

object Main {
    @JvmStatic
    fun main(vararg args: String) {
        if (args.isEmpty()) {
            println("ERROR: the list of arguments is empty")
            exitProcess(1)
        }

        println("Java version: ${System.getProperty("java.version")}")
        println("Arguments: ${args.toList()}")
    }
}

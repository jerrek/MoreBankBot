package ru.nox.sisabot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class ScoreBotApplication

fun main(args: Array<String>) {
    runApplication<ScoreBotApplication>(*args)
}

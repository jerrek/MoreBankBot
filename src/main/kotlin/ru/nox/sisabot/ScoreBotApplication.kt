package ru.nox.sisabot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["ru.nox.sisabot"])
open class ScoreBotApplication

fun main(args: Array<String>) {
    runApplication<ScoreBotApplication>(*args)
}

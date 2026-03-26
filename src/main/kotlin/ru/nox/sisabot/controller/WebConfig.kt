package ru.nox.sisabot.controller

import ProbeController
import WebhookController
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WebConfig {

    @Bean
    fun webhookController(botController: BotController): WebhookController {
        return WebhookController(botController)
    }

     @Bean
     fun probeController(): ProbeController {
         return ProbeController()
     }
}
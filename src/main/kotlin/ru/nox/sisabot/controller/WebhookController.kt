import jakarta.annotation.PostConstruct
import org.springframework.web.bind.annotation.*
import org.telegram.telegrambots.meta.api.objects.Update
import ru.nox.sisabot.controller.BotController
import java.time.LocalDateTime

@RestController
class WebhookController(private val bot: BotController) {

    @PostConstruct
    fun registerBot() {
        println("Registering webhook")
    }

    @PostMapping("/webhook")
    fun webhook(@RequestBody update: Update): String {
        // Логируем, что запрос пришёл (будет видно в логах Render)
        println("${LocalDateTime.now()} Получен webhook запрос от Telegram")
        bot.onUpdateReceived(update)
        return "OK"
    }

    // Добавим GET для проверки, что контроллер вообще работает
    @GetMapping("/webhook")
    fun test(): String {
        return "Webhook endpoint is alive! Use POST for Telegram updates."
    }
}
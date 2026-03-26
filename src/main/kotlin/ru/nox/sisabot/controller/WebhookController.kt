import org.springframework.web.bind.annotation.*
import org.telegram.telegrambots.meta.api.objects.Update
import ru.nox.sisabot.controller.BotController

@RestController
class WebhookController(private val bot: BotController) {

    @PostMapping("/webhook")
    fun webhook(@RequestBody update: Update) {
        bot.onUpdateReceived(update)
    }
}
import jakarta.annotation.PostConstruct
import org.springframework.web.bind.annotation.*
import org.telegram.telegrambots.meta.api.objects.Update
import ru.nox.sisabot.controller.BotController
import java.time.LocalDateTime

@RestController
class ProbeController() {

    @PostConstruct
    fun Initialization() {
        println("Application started!")
    }

    // Добавим GET для проверки, что контроллер вообще работает
    @GetMapping("/ping")
    fun test(): String {
        return "Webhook endpoint is alive! Use POST for Telegram updates."
    }
}
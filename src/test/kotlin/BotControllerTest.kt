package ru.nox.sisabot.controller

import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class BotControllerTest {

    private lateinit var bot: BotController

    @BeforeEach
    fun setup() {
        bot = spyk(BotController(), recordPrivateCalls = true)
        // Заглушаем execute, чтобы бот не стучался в Telegram
        every { bot.execute(any<SendMessage>()) } returns mockk()
    }

    private fun buildUpdate(text: String, chatId: Long = 1234L): Update {
        val message = mockk<Message>(relaxed = true)
        every { message.hasText() } returns true
        every { message.text } returns text
        every { message.chatId } returns chatId

        val update = mockk<Update>(relaxed = true)
        every { update.hasMessage() } returns true
        every { update.message } returns message
        return update
    }

    @Test
    fun `должен отправлять приветствие на start`() {
        val update = buildUpdate("/start")

        bot.onUpdateReceived(update)

        verify {
            bot.execute(match<SendMessage> {
                it.text.contains("Привет!")
            })
        }
    }

//    @Test
//    fun `должен предупреждать если нет аргумента после points`() {
//        val update = buildUpdate("/points")
//
//        bot.onUpdateReceived(update)
//
//        verify {
//            bot.execute(match<SendMessage> {
//                it.text.contains("Пожалуйста, введи /points твой_никнейм")
//            })
//        }
//    }

    @Test
    fun `парсинг JSON ответа в UserPointsResponse`() = runTest {
        val json = """{"username":"Иванов Иван","score":42}"""

        val result = kotlinx.serialization.json.Json.decodeFromString(
            BotController.UserPointsResponse.serializer(),
            json
        )

        assertEquals("Иванов Иван", result.username)
        assertEquals(42, result.score)
    }

    @Test
    fun `неизвестная команда должна возвращать подсказку`() {
        val update = buildUpdate("/abracadabra")

        bot.onUpdateReceived(update)

        verify {
            bot.execute(match<SendMessage> {
                it.text == "Неизвестная команда."
            })
        }
    }
}
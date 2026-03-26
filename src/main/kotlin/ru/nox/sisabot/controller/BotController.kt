package ru.nox.sisabot.controller

import jakarta.annotation.PostConstruct
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDateTime

@Service
class BotController : TelegramLongPollingBot() {

    @PostConstruct
    fun registerBot() {
        try {
//            Отключает лонг полинг
//            val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
//            botsApi.registerBot(this)
            val commands = listOf(
                BotCommand("start", "Инструкция"),
                BotCommand("points", "/points ФИО или название команды")
            )
            execute(SetMyCommands(commands, BotCommandScopeDefault(), null))
            println("Бот успешно зарегистрирован!")
        } catch (e: TelegramApiException) {
            println("Ошибка регистрации бота:")
            e.printStackTrace()
        }

        try {
            // Удаляем старый webhook (на всякий случай)
            val deleteWebhook = DeleteWebhook()
            execute(deleteWebhook)
            println("✅ Старый webhook удалён")
            val setWebhook = SetWebhook()
            setWebhook.setUrl("https://morebankbot.onrender.com/webhook")
            execute(setWebhook)
            println("Webhook установлен на https://morebankbot.onrender.com/webhook")
        } catch (e: TelegramApiException) {
            println("Ошибка установки webhook:")
            e.printStackTrace()
        }
    }

    private val gasUrl = "https://script.google.com/macros/s/AKfycbxoNl4iSEy62qb6ALDvKt7znHncYFlaHYSUP5QnljNbeOGWri9sj2TYZJS5RlhnHdoTTg/exec"

    override fun getBotUsername(): String = "MoreBank_bot"

    override fun getBotToken(): String =
        System.getenv("TELEGRAM_TOKEN")

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage() && update.message.hasText()) {
            val text = update.message.text
            val chatId = update.message.chatId.toString()
            println("${LocalDateTime.now()} Получена команда! chatId - $chatId text - $text")

            when {
                text.startsWith("/start") -> {
                    println("${LocalDateTime.now()} Получена команда start")
                    sendWelcomeKeyboard(chatId)
                    sendMessage(chatId, "Привет! Чтобы узнать свои баллы, введи /points ФИО или название команды")
                }

                text.startsWith("/points") -> {
                    println("${LocalDateTime.now()} Получена команда points")
                    val args = text.split(" ")

                    if (args.size == 1) {
                        println("${LocalDateTime.now()} Введено мало символов")
                        sendMessage(chatId, "Пожалуйста, введи /points ФИО или название команды")
                    } else {
                        val username = args.drop(1).joinToString(" ")
                        println("${LocalDateTime.now()} username = $username")

                        // Синхронный HTTP-запрос через HttpURLConnection (без Ktor и корутин)
                        try {
                            println("${LocalDateTime.now()} Отправляется запрос в гугл таблицы")

                            // Кодируем username для URL
                            val encodedUsername = URLEncoder.encode(username, "UTF-8")
                            val url = URL("$gasUrl?username=$encodedUsername")
                            val connection = url.openConnection() as HttpURLConnection

                            connection.requestMethod = "GET"
                            connection.connectTimeout = 15000
                            connection.readTimeout = 15000

                            val responseCode = connection.responseCode
                            println("${LocalDateTime.now()} HTTP код ответа: $responseCode")

                            val rawResponse = if (responseCode == HttpURLConnection.HTTP_OK) {
                                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                                    reader.readText()
                                }
                            } else {
                                // Если ошибка, читаем поток ошибок
                                BufferedReader(InputStreamReader(connection.errorStream)).use { reader ->
                                    reader.readText()
                                }
                            }

                            connection.disconnect()

                            println("${LocalDateTime.now()} Ответ от гугл получен: $rawResponse")

                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                println("${LocalDateTime.now()} Формирование ответа")
                                val userPointsResponse = Json.decodeFromString<UserPointsResponse>(rawResponse)

                                println("${LocalDateTime.now()} Отправка ответа пользователю")
                                sendMessage(chatId, "$username - ${userPointsResponse.score}")

                                println("${LocalDateTime.now()} Ответ в бот на запрос отправлен")
                            } else {
                                println("${LocalDateTime.now()} Ошибка HTTP: $responseCode")
                                sendMessage(chatId, "Ошибка сервера: $responseCode")
                            }

                        } catch (e: Exception) {
                            println("${LocalDateTime.now()} ОШИБКА: ${e.javaClass.simpleName} - ${e.message}")
                            e.printStackTrace()
                            sendMessage(chatId, "Ошибка при запросе: ${e.message}")
                        }
                    }
                }
                else -> {
                    sendMessage(chatId, "Неизвестная команда.")
                }
            }
        }
    }

    private fun sendMessage(chatId: String, text: String) {
        val message = SendMessage(chatId, text)
        try {
            execute(message)
        } catch (e: TelegramApiException) {
            println("${LocalDateTime.now()} Ошибка отправки сообщения: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun sendWelcomeKeyboard(chatId: String) {
        val keyboardMarkup = ReplyKeyboardMarkup(
            listOf(
                KeyboardRow().apply {
                    add(KeyboardButton("/points"))
                }
            )
        )
        keyboardMarkup.resizeKeyboard = true
        keyboardMarkup.oneTimeKeyboard = true

        val message = SendMessage(chatId, "👋 Добро пожаловать!")
        message.replyMarkup = keyboardMarkup

        try {
            execute(message)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    @Serializable
    data class UserPointsResponse(
        val username: String? = null,
        val score: Int? = null
    )
}
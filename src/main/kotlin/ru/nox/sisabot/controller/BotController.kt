package ru.nox.sisabot.controller

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import java.time.LocalDateTime
@Service
class BotController : TelegramLongPollingBot() {

    @PostConstruct
    fun registerBot() {
        try {
            val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
            botsApi.registerBot(this)
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
    }

    private val gasUrl =
        "https://script.google.com/macros/s/AKfycbxoNl4iSEy62qb6ALDvKt7znHncYFlaHYSUP5QnljNbeOGWri9sj2TYZJS5RlhnHdoTTg/exec"

    override fun getBotUsername(): String = "MoreBank_bot"

    override fun getBotToken(): String =
        System.getenv("TELEGRAM_TOKEN")

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage() && update.message.hasText()) {
            val text = update.message.text
            val chatId = update.message.chatId.toString()
            when {
                text.startsWith("/start") -> {
                    sendWelcomeKeyboard(chatId)
                    sendMessage(chatId, "Привет! Чтобы узнать свои баллы, введи /points ФИО или название команды")
                }

                text.startsWith("/points") -> {
                    val args = text.split(" ")
                    if (args.size == 1) {
                        sendMessage(chatId, "Пожалуйста, введи /points ФИО или название команды")
                    } else {
                        var username = ""
                        for (i in args.indices) {
                            if (i == 0) {
                                continue
                            }
                            username += "${args[i]} "
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            val httpClient = HttpClient(CIO) {
                                install(ContentNegotiation) {
                                    json(Json {
                                        ignoreUnknownKeys = true
                                    })
                                }
                            }
                            val rawResponse: String = httpClient.get(gasUrl) {
                                parameter("username", username)
                            }.bodyAsText()
                            println(LocalDateTime.now().toString() + " " + rawResponse)

                            try {
                                val userPointsResponse = Json.decodeFromString<UserPointsResponse>(rawResponse)
                                sendMessage(chatId, "$username - ${userPointsResponse.score}")
                            } catch (e: Exception) {
                                sendMessage(chatId, "Пользователь не найден")
                            }
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
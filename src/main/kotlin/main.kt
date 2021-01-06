import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import com.github.kotlintelegrambot.logging.LogLevel
import com.github.kotlintelegrambot.network.fold
import table.joinStringArray
import java.io.File

fun main() {

    val chatsToTableManagers: MutableMap<Long, TableManager> = mutableMapOf()
    val reader = File("token.token").bufferedReader()
    val readToken = reader.use { it.readLine() }

    val bot = bot {
        token = readToken
        timeout = 30
        logLevel = LogLevel.All()

        dispatch {
            command("start") {

                val result = bot.sendMessage(chatId = update.message!!.chat.id, text = "Bot started. Enter /help")

                result.fold({
                    // do something here with the response
                }, {
                    // do something with the error
                })
            }

            command("init_table") {
                val tableString = args[0]
                if (chatsToTableManagers[message.chat.id] == null) {
                    chatsToTableManagers[message.chat.id] = TableManager(message.chat.id)
                }
                // bot.sendMessage(chatId = message.chat.id, text = "Some weird shit happened")
                chatsToTableManagers[message.chat.id]?.updateTable(tableString)
                    ?: throw Exception("How it became null???")
                bot.sendMessage(chatId = message.chat.id, text = "Table updated successfully. Now enter /get_info")
            }

            command("get_info") {
                val username = joinStringArray(args, " ")
                if (chatsToTableManagers[message.chat.id] == null) {
                    bot.sendMessage(
                        chatId = message.chat.id,
                        text = "You have to init table first (use /init_table <link>)"
                    )
                    return@command
                }
                bot.sendMessage(chatId = message.chat.id, text = username)
                val textInfo = chatsToTableManagers[message.chat.id]?.getAllInformation(username)
                    ?: throw Exception("How it became null???")
//                bot.sendMessage(chatId = message.chat.id, text = textInfo)
                splitAndSend(bot, message.chat.id, textInfo)
            }

            command("get_contests") {
                if (chatsToTableManagers[message.chat.id] == null) {
                    bot.sendMessage(
                        chatId = message.chat.id,
                        text = "You have to init table first (use /init_table <link>)"
                    )
                    return@command
                }
                bot.sendMessage(chatId = message.chat.id, text = "Sending all contests information...")
                val textContestsInfo = chatsToTableManagers[message.chat.id]?.getContestsInformation()
                    ?: throw Exception("How it became null???")
                bot.sendMessage(chatId = message.chat.id, text = textContestsInfo)
            }

            command("get_magic_number") {
                if (chatsToTableManagers[message.chat.id] == null) {
                    bot.sendMessage(
                        chatId = message.chat.id,
                        text = "You have to init table first (use /init_table <link>)"
                    )
                    return@command
                }
                val answer = chatsToTableManagers[message.chat.id]?.getMagicExamNumber()
                    ?: throw Exception("How it became null???")
                bot.sendMessage(chatId = message.chat.id, text = answer)
            }

            command("get_magic_list") {
                val howMuch = 30
                if (chatsToTableManagers[message.chat.id] == null) {
                    bot.sendMessage(
                        chatId = message.chat.id,
                        text = "You have to init table first (use /init_table <link>)"
                    )
                    return@command
                }
                val answer = chatsToTableManagers[message.chat.id]?.getTheSolveList(howMuch)!!
                splitAndSend(bot, message.chat.id, answer)
            }

            command("inline_buttons") {
                val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                    listOf(InlineKeyboardButton.CallbackData(text = "Test Inline Button", callbackData = "testButton")),
                    listOf(InlineKeyboardButton.CallbackData(text = "Show alert", callbackData = "showAlert"))
                )
                bot.sendMessage(
                    chatId = message.chat.id,
                    text = "Hello, inline buttons!",
                    replyMarkup = inlineKeyboardMarkup
                )
            }

            command("user_buttons") {
                val keyboardMarkup = KeyboardReplyMarkup(keyboard = generateUsersButton(), resizeKeyboard = true)
                bot.sendMessage(
                    chatId = message.chat.id,
                    text = "Hello, users buttons!",
                    replyMarkup = keyboardMarkup
                )
            }

            command("help") {
                val help = """
                    Firstly you have to enter the table of you parallel:
                    /init_table <link to your table>
                    Then you can get the list of problems to solve:
                    /get_info <Surname> <Name>
                """.trimIndent()
                bot.sendMessage(chatId = message.chat.id, text = help)
            }

            text("ping") {
                bot.sendMessage(chatId = message.chat.id, text = "Pong")
            }

            telegramError {
                println(error.getErrorMessage())
            }
        }
    }

    bot.startPolling()
}

fun generateUsersButton(): List<List<KeyboardButton>> {
    return listOf(
        listOf(KeyboardButton("Request location (not supported on desktop)", requestLocation = true)),
        listOf(KeyboardButton("Request contact", requestContact = true))
    )
}
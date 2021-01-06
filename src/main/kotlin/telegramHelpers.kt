import com.github.kotlintelegrambot.Bot

fun splitAndSend(bot: Bot, chatId: Long, message: String) {
    var prevN = 0
    var prevS = 0
    var prevB = 0
    for (ind in message.indices) {
        if (message[ind] == '\n') prevN = ind
        if (message[ind] == ' ') prevS = ind
        if (ind - prevB >= 4000) {
            if (prevN != 0) {
                bot.sendMessage(chatId=chatId, text=message.substring(prevB, prevN))
                prevB = prevN + 1
                prevN = 0
                if (prevS <= prevB) prevS = 0
            } else if (prevS != 0) {
                bot.sendMessage(chatId=chatId, text=message.substring(prevB, prevS))
                prevB = prevS + 1
                prevN = 0
                prevS = 0
            } else {
                bot.sendMessage(chatId=chatId, text=message.substring(prevB, ind))
                prevB = ind
                prevN = 0
                prevS = 0
            }
        }
    }
    if (prevB != message.indices.last) bot.sendMessage(chatId=chatId, text=message.substring(prevB))
}
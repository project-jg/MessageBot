package io.jigoo.message.demo

import com.linecorp.bot.model.event.Event
import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.message.Message
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.spring.boot.annotation.EventMapping
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler
import org.springframework.stereotype.Service

@Service
@LineMessageHandler
class MessageEventHandler(private val actionDispatcher: ActionDispatcher) {

    @EventMapping
    fun handleTextMessageEvent(event: MessageEvent<TextMessageContent>): Message {
        println("event: $event")

        return try {
            TextMessage(actionDispatcher.dispatch(event))
        } catch (e: Exception) {
            TextMessage(e.message)
        }
    }

    @EventMapping
    fun handleDefaultMessageEvent(event: Event) {
        println("event: $event")
    }
}
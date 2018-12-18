@file:Suppress("UnstableApiUsage")

package io.jigoo.message.demo

import com.linecorp.bot.client.LineMessagingClient
import com.linecorp.bot.model.event.Event
import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.message.Message
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.spring.boot.annotation.EventMapping
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*

@Service
@LineMessageHandler
class MessageEventHandler(private val actionDispatcher: ActionDispatcher) {

    @EventMapping
    fun handleTextMessageEvent(event: MessageEvent<TextMessageContent>): Message {
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

@Component
@ConfigurationProperties("line.bot")
class LineBotConfig {
    lateinit var channelToken: String
}

@Configuration
@EnableConfigurationProperties(LineBotConfig::class)
class LineMessagingClientConfig(val lineBotConfig: LineBotConfig) {

    fun lineMessagingClient(): LineMessagingClient {
        return LineMessagingClient
            .builder(lineBotConfig.channelToken)
            .build()
    }

}

@RestController
class GameConfigController(private val gameConfigService: GameConfigService) {

    @GetMapping("/gameConfigs/current")
    fun getCurrentGame(): GameConfig {
        return gameConfigService.getCurrentGame()
            .takeIf { it.isPresent }
            ?.let { it.get() }
            ?: throw IllegalStateException("게임이 존재하지 않습니다.")
    }

    @PutMapping("/gameConfigs/current/{turn}")
    fun updateCurrentTurn(@PathVariable turn: Int): GameConfig {
        return gameConfigService.getGame(turn)
            .takeIf { it.isPresent }
            ?.let { it.get() }
            ?.apply {
                gameConfigService.currentTurn = turn
            } ?: throw IllegalStateException("게임이 존재하지 않습니다.")
    }
}

@RestController
class TeamController(private val teamService: TeamService) {

    @PostMapping("/teams")
    fun save(teamName: TeamName, userId: String): Team {
        return teamService.saveTeam(teamName, userId)
    }

    @GetMapping("/teams")
    fun getAllTeams(): MutableIterable<Team> {
        return teamService.getAllTeams()
    }
}

@RestController
class GameController(private val gameService: GameService) {

    @PostMapping("/turns/current/games")
    fun save(teamName: TeamName, selected: String): Game {
        return gameService.save(teamName = teamName, selected = selected)
    }

    @GetMapping("/turns/{turn}/games")
    fun getGames(@PathVariable turn: Int): List<Game> {
        return gameService.findByTurn(turn)
    }

    @GetMapping("/turns/current/games")
    fun getCurrentGames(): List<Game> {
        return gameService.findByTurn()
    }
}
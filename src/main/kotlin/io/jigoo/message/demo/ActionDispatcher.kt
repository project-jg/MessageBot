package io.jigoo.message.demo

import com.linecorp.bot.client.LineMessagingClient
import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.io.Serializable
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass

enum class TeamName(val info: String) {
    TECH_1("오디오기술개발 1"),
    TECH_2("오디오기술개발 2"),
    APP_DEV("오디오서비스개발 Android + iOS"),
    SERVICE_1("오디오서비스개발 서버 1"),
    SERVICE_2("오디오서비스개발 서버 2 + 키즈개발")
}

@Service
class ActionDispatcher(private val teamInitActionHandler: TeamInitActionHandler, private val numberActionHandler: NumberActionHandler) {
    fun dispatch(event: MessageEvent<TextMessageContent>) =
        if (event.message.text.toIntOrNull() == null)
            teamInitActionHandler.handle(event)
        else
            numberActionHandler.handle(event)
}

interface ActionHandler {
    fun handle(event: MessageEvent<TextMessageContent>): String
}

@Service
class TeamInitActionHandler(
    private val teamService: TeamService
) : ActionHandler {

    override fun handle(event: MessageEvent<TextMessageContent>): String {
        val teamName = try {
            TeamName.valueOf(event.message.text.toUpperCase())
        } catch (e: Exception) {
            throw RuntimeException("!!! 팀 이름을 잘못 입력하셨습니다! 다시 입력해주세요.");
        }

        return teamService.findByUserId(event.source.userId).let {
            if (it != null) {
                "!!! 이미 ${it.teamName} 으로 등록했습니다."
            } else {
                teamService.saveTeam(teamName = teamName, userId = event.source.userId)
                "${teamName}(${teamName.info}) 팀으로 등록되었습니다! ^^"
            }
        }
    }
}


@Service
class NumberActionHandler(
    private val teamInitRepository: TeamInitRepository,
    private val gameService: GameService,
    private val gameConfigService: GameConfigService
) : ActionHandler {

    override fun handle(event: MessageEvent<TextMessageContent>): String {
        val team = teamInitRepository.findByUserId(event.source.userId)

        return team?.teamName?.let {
            gameService.save(
                teamName = it,
                selected = event.message.text.toIntOrNull() ?: throw RuntimeException("!!! 숫자만 입력해 주세요.")
            )
        }?.let {
            "${it.selected} 를 선택하셨네요 ^^ 결과를 기대해주세요 >_<"
        } ?: throw RuntimeException("!!! 먼저 팀 선택을 해주세요!")
    }
}

@Service
class GameConfigService(val gameConfigRepository: GameConfigRepository) {
    var currentTurn: Int = 1
    fun getGame(turn: Int): Optional<GameConfig> {
        return gameConfigRepository.findById(turn)
    }

    fun getCurrentGame(): Optional<GameConfig> {
        return gameConfigRepository.findById(currentTurn)
    }
}

@Service
class TeamService(
    val teamInitRepository: TeamInitRepository,
    val lineMessagingClient: LineMessagingClient
) {
    fun saveTeam(teamName: TeamName, userId: String): Team {
        val userProfile = lineMessagingClient.getProfile(userId).get()

        return teamInitRepository.save(
            Team(
                teamName = teamName,
                userId = userId,
                userName = userProfile.displayName,
                userPicture = userProfile.pictureUrl
            )
        )
    }

    fun findByUserId(userId: String): Team? {
        return teamInitRepository.findByUserId(userId)
    }

    fun getAllTeams(): MutableIterable<Team> {
        return teamInitRepository.findAll()
    }
}

@Service
class GameService(
    val gameConfigService: GameConfigService,
    val gameRepository: GameRepository
) {

    fun save(teamName: TeamName, selected: Int): Game {
        return gameRepository.save(
            Game(
                turn = gameConfigService.currentTurn,
                teamName = teamName,
                selected = selected,
                updated = Date()
            )
        )
    }

    fun findByTurn(turn: Int): List<Game> {
        return gameRepository.findByTurn(turn)
    }

    fun findByTurn(): List<Game> {
        return gameRepository.findByTurn(gameConfigService.currentTurn)
    }
}

@Repository
interface TeamInitRepository : CrudRepository<Team, String> {
    fun findByUserId(userId: String): Team?
}

@Repository
interface GameRepository : CrudRepository<Game, Int> {
    fun findByTurn(turn: Int): List<Game>
}

@Repository
interface GameConfigRepository : CrudRepository<GameConfig, Int>

@Entity
data class Team(
    @Id val teamName: TeamName,
    val userId: String,
    val userName: String,
    val userPicture: String
) {
    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}

@Entity(name = "game_config")
data class GameConfig(
    @Id val turn: Int,
    val question: String,
    val answer: Int
)

@Entity
@IdClass(GameKey::class)
data class Game(
    @Id val turn: Int,
    @Id val teamName: TeamName,
    val selected: Int,
    val updated: Date
)

class GameKey : Serializable {
    var turn: Int = 0
    lateinit var teamName: TeamName
}


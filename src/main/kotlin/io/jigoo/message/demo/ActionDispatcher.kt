package io.jigoo.message.demo

import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import javax.persistence.*

enum class TeamNames(name: String) {
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
class TeamInitActionHandler(private val teamInitRepository: TeamInitRepository) : ActionHandler {

    override fun handle(event: MessageEvent<TextMessageContent>): String {
        val teamName = try {
            TeamNames.valueOf(event.message.text.toUpperCase())
        } catch (e: Exception) {
            throw RuntimeException("(rage) 팀 이름을 잘못 입력하셨습니다! 다시 입력해주세요.");
        }
        val userId = event.source.userId

        teamInitRepository.apply {
            save(
                Team(
                    id = findByTeamName(teamName)?.id,
                    teamName = teamName,
                    userId = userId
                )
            )
        }

        return "${teamName}(${teamName.name}) 팀으로 등록되었습니다! (smile)"
    }
}

@Service
class NumberActionHandler(
    private val teamInitRepository: TeamInitRepository,
    private val gameRepository: GameRepository,
    private val gameConfigRepository: GameConfigRepository
) : ActionHandler {

    /*
    TODO 숫자를 받는다
    팀을 선택 안한사람이면 실패
    (숫자에 대한 유효성 체크도 ?)

    게임회차, 팀, 선택한 숫자를 저장
     */
    override fun handle(event: MessageEvent<TextMessageContent>): String {
        val team = teamInitRepository.findByUserId(event.source.userId)

        return team?.teamName?.let {
            gameRepository.save(
                Game(
                    teamName = it,
                    selected = event.message.text.toIntOrNull() ?: throw RuntimeException("(rage) 숫자만 입력해 주세요."),
                    turn = gameConfigRepository.findAll().first()?.currentTurn ?: throw RuntimeException("(rage) 진행중인 게임이 없습니다.")
                )
            )
        }?.let {
            "${it.selected} 를 선택하셨네요 (smile) 결과를 기대해주세요 (blush)"
        } ?: throw RuntimeException("(rage) 먼저 팀 선택을 해주세요!")
    }
}

@Repository
interface TeamInitRepository : CrudRepository<Team, Int> {
    fun findByUserId(userId: String): Team?
    fun findByTeamName(teamName: TeamNames): Team?
}

@Repository
interface GameRepository : CrudRepository<Game, Int>

@Repository
interface GameConfigRepository : CrudRepository<GameConfig, Int>

@Entity
data class Team(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int? = null,
    @Column(unique = true) val teamName: TeamNames,
    @Column(unique = true) val userId: String
)

@Entity
data class GameConfig(
    @Id val id: Int = 0,
    @Column(unique = true) val currentTurn: Int,
    val answer: Int
)

@Entity
data class Game(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int? = null,
    val teamName: TeamNames,
    val turn: Int,
    val selected: Int
)


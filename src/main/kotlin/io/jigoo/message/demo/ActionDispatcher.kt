package io.jigoo.message.demo

import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import javax.persistence.*

enum class Action {
    TEAM_INIT,
    NUMBER
}

enum class TeamNames {
    TECH_1,
    TECH_2,
    APP_DEV,
    SERVER_DEV,
    KIDS_DEV
}

@Service
class ActionDispatcher(private val teamInitActionHandler: TeamInitActionHandler, private val numberActionHandler: NumberActionHandler) {
    fun dispatch(event: MessageEvent<TextMessageContent>) =
        if (event.message.text.toIntOrNull() == null) teamInitActionHandler.handle(event) else numberActionHandler.handle(event)
}

interface ActionHandler {
    fun handle(event: MessageEvent<TextMessageContent>): String
}

@Service
class TeamInitActionHandler(private val teamInitRepository: TeamInitRepository) : ActionHandler {

    override fun handle(event: MessageEvent<TextMessageContent>): String {
        val teamName = TeamNames.valueOf(event.message.text.toUpperCase())
        val userId = event.source.userId

        //TODO 팀과 유저아이디를 스토리지에 저장한다
        teamInitRepository.apply {
            save(Team(id = findByTeamName(teamName)?.id, teamName = teamName, userId = userId))
        }

        //TODO 답장을 보내준다
        return "${teamName} 으로 등록되었습니다! ^^"
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
                    selected = event.message.text.toIntOrNull() ?: throw RuntimeException("잘못입력했음"),
                    turn = gameConfigRepository.findAll().first()?.currentTurn ?: throw RuntimeException("게임 없음")
                )
            )
        }?.let { "${it.selected} 를 선택하셨네요" } ?: throw RuntimeException("팀 선택을 안했음")
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


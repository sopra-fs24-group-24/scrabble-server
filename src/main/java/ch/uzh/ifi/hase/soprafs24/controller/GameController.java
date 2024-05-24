package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Tile;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.GameDTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
public class GameController 
{

    private final GameService gameService;
    @Autowired
    private final UserService userService;

    GameController(GameService gameService, UserService userService) 
    {
        this.gameService = gameService;
        this.userService=userService;
    }

    @GetMapping("/games/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public GameGetDTO getGame(@PathVariable("gameId") Long gameId, @RequestHeader("token") String token)
    {
        User foundUser = userService.isTokenValid(token);
        Game foundGame = gameService.getGameParams(gameId);
        GameGetDTO gameGetDTO = GameDTOMapper.INSTANCE.convertEntityToGameGetDTO(foundGame);
        gameService.transformUsersIntoUsersSlim(gameGetDTO, foundGame);
        gameService.removeHandsFromOtherPlayers(gameGetDTO, foundUser.getId());
        return gameGetDTO;
    }

    @PostMapping("moves/words/{gameId}")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public List<Tile> placeWordOnPlayfield(@PathVariable Long gameId, @RequestBody GamePostDTO gamePostDTO,@RequestHeader("token") String token) 
    {
        userService.isTokenValid(token);
        Game updatedGame = GameDTOMapper.INSTANCE.convertGamePostDTOToEntity(gamePostDTO);
        userService.authenticateUser(updatedGame.getCurrentPlayer(), token);
        return gameService.placeTilesOnBoard(updatedGame);
    }

    @PostMapping("moves/swaps/{gameId}/{userId}/{handId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<Tile> swapTilesOfPlayer(@PathVariable Long gameId, @PathVariable Long userId,
    @PathVariable Long handId, @RequestBody List<Tile> inputTiles,@RequestHeader("token") String token) 
    {
        userService.isTokenValid(token);
        userService.authenticateUser(userId, token);
        // returns new hand (exchanged tiles + remaining tiles)
        return gameService.swapTiles(gameId, userId, handId, inputTiles);

    }

    @PostMapping("moves/skip/{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void skipTurn(@PathVariable Long gameId,@RequestHeader("token") String token) 
    {
        User user = userService.isTokenValid(token);
        gameService.skipTurn(user, gameId);
    }

    @PostMapping("moves/contestations/{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void contestWord(@PathVariable Long gameId, @RequestBody Map<String, Boolean> requestBody, @RequestHeader("token") String token)
    {
        User user = userService.isTokenValid(token);
        boolean wordContested = requestBody.get("decisionContesting");
        gameService.contestWord(gameId, user, wordContested);
    }

    @PostMapping("/definitions")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> getDefinitions(@RequestBody  List<String> requestBody, @RequestHeader("token") String token) {
        userService.isTokenValid(token);
        return gameService.getDefinition(requestBody);
    }
}

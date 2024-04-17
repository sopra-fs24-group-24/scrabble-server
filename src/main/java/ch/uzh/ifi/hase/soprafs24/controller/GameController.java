package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Tile;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.GameDTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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
    public Game getGame(@PathVariable("gameId") Long gameId,@RequestParam(required=false) String token) 
    {
        userService.isTokenValid(token);
        
        Game requestedGame=gameService.getGameParams(gameId);
        return requestedGame;
    }

    @PostMapping("moves/words/{gameId}")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public void placeWordOnPlayfield(@PathVariable Long gameId, @RequestBody GamePostDTO gamePostDTO) {
        Game updatedGame = GameDTOMapper.INSTANCE.convertGamePostDTOToEntity(gamePostDTO);
        gameService.placeTilesOnBoard(updatedGame);
    }

    @PostMapping("moves/swaps/{gameId}/{userId}/{handId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<Tile> swapTilesOfPlayer(@PathVariable Long gameId, @PathVariable Long userId,
                                        @PathVariable Long handId, @RequestBody List<Tile> inputTiles) {
        // returns new hand (exchanged tiles + remaining tiles)
        return gameService.swapTiles(gameId, userId, handId, inputTiles);

    }

    @PostMapping("moves/skip/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    public void skipTurn(@PathVariable Long gameId, @RequestParam String token) {
        User user = userService.isTokenValid(token);
        gameService.skipTurn(user, gameId);
    }
}

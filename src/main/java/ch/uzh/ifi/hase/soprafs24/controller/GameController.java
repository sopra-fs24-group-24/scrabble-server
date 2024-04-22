package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
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

import java.util.ArrayList;
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
    public GameGetDTO getGame(@PathVariable("gameId") Long gameId, @RequestParam(required=false) String token)
    {
        userService.isTokenValid(token);
        
        GameGetDTO gameGetDTO = GameDTOMapper.INSTANCE.convertEntityToGameGetDTO(gameService.getGameParams(gameId));
        return gameGetDTO;
    }

    @PostMapping("moves/words/{gameId}")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public List<Tile> placeWordOnPlayfield(@PathVariable Long gameId, @RequestBody GamePostDTO gamePostDTO) {
        Game updatedGame = GameDTOMapper.INSTANCE.convertGamePostDTOToEntity(gamePostDTO);
        return gameService.placeTilesOnBoard(updatedGame);
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

    // TODO: temporary endpoint for testing purposes: Delete when not needed anymore
    @PostMapping("/quiek")
    @ResponseStatus(HttpStatus.OK)
    public Lobby startGame() 
    {
        Lobby lobby=new Lobby();
        lobby.setId(1L);
        lobby.setNumberOfPlayers(2);

        User user = new User();
        user.setId(1L);
        user.setUsername("Anna");
        user.setPassword("1234");
        userService.createUser(user);

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("Bert");
        user2.setPassword("1234");
        userService.createUser(user2);

        Game game= new Game();

        List<User> players=new ArrayList<User>();
        players.add(user);
        players.add(user2);
        lobby.setPlayers(players);

        game.initialiseGame(lobby.getPlayers());

        lobby.setGameOfLobby(game);

        return lobby;
    }

}

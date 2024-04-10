package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class GameController 
{

    private final GameService gameService;

    GameController(GameService gameService) 
    {
        this.gameService = gameService;
    }

    @RequestMapping("/games/{gameId}")
    @ResponseBody
    public Game getGame(@PathVariable("gameId") Long gameId,@RequestParam(required=false) String token) 
    {
        // TODO: Need to check token first
        Game requestedGame=gameService.getGameParams(gameId);
        return requestedGame;
    }
        
}

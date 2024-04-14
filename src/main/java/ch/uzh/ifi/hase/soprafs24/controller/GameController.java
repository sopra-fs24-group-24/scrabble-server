package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    public Game getGame(@PathVariable("gameId") Long gameId,@RequestParam(required=false) String token) 
    {
        userService.isTokenValid(token);
        
        Game requestedGame=gameService.getGameParams(gameId);
        return requestedGame;
    }      
}

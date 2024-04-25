package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Highscore;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.HighscoreGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.HighscorePostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LogoutPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.HighscorecDTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.HighscoreService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class HighscoreController 
{

  private final HighscoreService highscoreService;

  HighscoreController(HighscoreService highscoreService) 
  {
    this.highscoreService = highscoreService;
  }

  @GetMapping("/highscores")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<HighscoreGetDTO> getAllHighscores(@RequestParam(required=false) String token) 
  {
    //userService.isTokenValid(token);

    // fetUserch all users in the internal representation
    List<Highscore> highscores = highscoreService.getHighscores();
    List<HighscoreGetDTO> highscoreGetDTOs = new ArrayList<>();

    for (Highscore highscore : highscores) 
    {
        highscoreGetDTOs.add(HighscorecDTOMapper.INSTANCE.convertEntityToHighscoreGetDTO(highscore));
    }
    return highscoreGetDTOs;
  }


}

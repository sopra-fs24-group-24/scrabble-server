package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.GameMode;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LogoutPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(GameController.class)
public class GameControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private GameService gameService;

  @Test
  public void getGame_validInputs() throws Exception 
  {
    // given
    Game game = new Game();

    game.setId(1L);
    game.setMode(GameMode.CLASSIC);
    game.setCurrentPlayer(0L);

    
    GameGetDTO gameGetDTO = new GameGetDTO();
    gameGetDTO.setId(1L);
    gameGetDTO.setMode(GameMode.CLASSIC);
    gameGetDTO.setCurrentPlayer(0L);
    
    given(gameService.getGameParams(Mockito.any())).willReturn(game);

// when/then -> do the request + validate the result
MockHttpServletRequestBuilder getRequest = get("/games/1")
.contentType(MediaType.APPLICATION_JSON)
.content(asJsonString(gameGetDTO));

// TODO: given(userService.isTokenValid(Mockito.any())).willReturn(user);


// then
mockMvc.perform(getRequest)
.andExpect(status().isOk())
.andExpect(jsonPath("$.id", is(game.getId().intValue())))
.andExpect(jsonPath("$.currentPlayer", is(game.getCurrentPlayer().intValue())))
.andExpect(jsonPath("$.mode", is(game.getMode().toString())));
  }

  @Test
  public void getGame_nonExistant() throws Exception 
  {
     // given
     Game game = new Game();

     game.setId(1L);
     game.setMode(GameMode.CLASSIC);
     game.setCurrentPlayer(0L);
 
     
     GameGetDTO gameGetDTO = new GameGetDTO();
     gameGetDTO.setId(1L);
     gameGetDTO.setMode(GameMode.CLASSIC);
     gameGetDTO.setCurrentPlayer(0L);
     
     ResponseStatusException e=new ResponseStatusException(HttpStatus.NOT_FOUND);

     given(gameService.getGameParams(Mockito.any())).willThrow(e);
 
 // when/then -> do the request + validate the result
 MockHttpServletRequestBuilder getRequest = get("/games/1")
 .contentType(MediaType.APPLICATION_JSON)
 .content(asJsonString(gameGetDTO));
 
 // TODO: given(userService.isTokenValid(Mockito.any())).willReturn(user);
 
 
 // then
 mockMvc.perform(getRequest)
 .andExpect(status().isNotFound());
  }

  /**
   * Helper Method to convert userPostDTO into a JSON string such that the input
   * can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   * 
   * @param object
   * @return string
   */
  private String asJsonString(final Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format("The request body could not be created.%s", e.toString()));
    }
  }
}
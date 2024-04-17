package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.GameMode;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Tile;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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

import static org.hamcrest.Matchers.hasSize;
import java.util.ArrayList;
import java.util.List;


import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

  @MockBean
  private UserService userService;

  ObjectMapper objectMapper = new ObjectMapper();

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

  @Test
  public void swapTiles_validInput_thenNewHandIsReturned() throws Exception {
      // given
      List<Tile> tilesToBeExchanged = new ArrayList<>();
      Tile tile1 = new Tile('X', 8);
      tile1.setId(1L);
      Tile tile2 = new Tile('Z', 10);
      tile2.setId(2L);
      tilesToBeExchanged.add(tile1);
      tilesToBeExchanged.add(tile2);

      List<Tile> returnedHand = new ArrayList<>();
      Tile returnedTile1 = new Tile('X', 8);
      returnedTile1.setId(3L);
      Tile returnedTile2 = new Tile('I',1);
      returnedTile2.setId(4L);
      Tile returnedTile3 = new Tile('K', 5);
      returnedTile3.setId(5L);
      Tile returnedTile4 = new Tile('D', 2);
      returnedTile4.setId(6L);
      Tile returnedTile5 = new Tile('L', 1);
      returnedTile5.setId(7L);
      Tile returnedTile6 = new Tile('P', 3);
      returnedTile6.setId(8L);
      Tile returnedTile7 = new Tile('R', 1);
      returnedTile7.setId(9L);

      returnedHand.add(returnedTile1);
      returnedHand.add(returnedTile2);
      returnedHand.add(returnedTile3);
      returnedHand.add(returnedTile4);
      returnedHand.add(returnedTile5);
      returnedHand.add(returnedTile6);
      returnedHand.add(returnedTile7);

      String convertedObject = asJsonString(returnedHand);
      JsonNode arrayRepresantation = objectMapper.readTree(convertedObject);

      given(gameService.swapTiles(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).willReturn(returnedHand);

      // when
      MockHttpServletRequestBuilder postRequest = post("/moves/swaps/{gameId}/{userId}/{handId}", 1, 1, 1)
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(tilesToBeExchanged));

      // then
      mockMvc.perform(postRequest).andExpect(status().isOk())
              .andExpect(content().json(arrayRepresantation.toString()));

  }

  @Test
  public void swapTiles_handOrGameNotFound_thenThrowError() throws Exception {
      // given
      List<Tile> tilesToBeExchanged = new ArrayList<>();
      Tile tile1 = new Tile('X', 8);
      tile1.setId(1L);
      Tile tile2 = new Tile('Z', 10);
      tile2.setId(2L);
      tilesToBeExchanged.add(tile1);
      tilesToBeExchanged.add(tile2);

      given(gameService.swapTiles(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

      // when
      MockHttpServletRequestBuilder postRequest = post("/moves/swaps/{gameId}/{userId}/{handId}", 1, 1, 1)
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(tilesToBeExchanged));

      // then
      mockMvc.perform(postRequest).andExpect(status().isNotFound())
              .andExpect(content().string(""));
  }

    @Test
    public void swapTiles_bagHasTooFewTiles_thenThrowError() throws Exception {
        // given
        List<Tile> tilesToBeExchanged = new ArrayList<>();
        Tile tile1 = new Tile('X', 8);
        tile1.setId(1L);
        Tile tile2 = new Tile('Z', 10);
        tile2.setId(2L);
        tilesToBeExchanged.add(tile1);
        tilesToBeExchanged.add(tile2);

        given(gameService.swapTiles(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.CONFLICT));

        // when
        MockHttpServletRequestBuilder postRequest = post("/moves/swaps/{gameId}/{userId}/{handId}", 1, 1, 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(tilesToBeExchanged));

        // then
        mockMvc.perform(postRequest).andExpect(status().isConflict())
                .andExpect(content().string(""));
    }

  @Test
  public void skipMove_invalidInput() throws Exception {
      User user = new User();
      user.setId(1L);

      ResponseStatusException response = new ResponseStatusException(HttpStatus.NOT_FOUND);

      given(userService.isTokenValid(Mockito.any())).willReturn(user);
      doThrow(response).when(gameService).skipTurn(Mockito.any(), Mockito.any());

      MockHttpServletRequestBuilder postRequest = post("/moves/skip/1?token=1")
              .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(postRequest)
              .andExpect(status().isNotFound());
  }

    @Test
    public void skipMove_validInput() throws Exception {
        User user = new User();
        user.setId(1L);

        given(userService.isTokenValid(Mockito.any())).willReturn(user);

        MockHttpServletRequestBuilder postRequest = post("/moves/skip/1?token=1")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isOk());
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
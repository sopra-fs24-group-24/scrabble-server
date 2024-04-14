package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GameService {

    private final Logger log = LoggerFactory.getLogger(GameService.class);

    private final GameRepository gameRepository;

    @Autowired
    public GameService(@Qualifier("gameRepository") GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }



    public Game getGameParams(Long gameId)
    {
      Optional<Game> game=gameRepository.findById(gameId);
      if(game.isPresent())
          return game.get();
  
      String baseErrorMessage = "The %s provided can not be found!";
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(baseErrorMessage, "Game ID"));
    }


}

package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Highscore;
import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.HighscoreRepository;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class HighscoreService 
{

    private final Logger log = LoggerFactory.getLogger(LobbyService.class);

    @Autowired
    private final HighscoreRepository highscoreRepository;

    @Autowired
    private final UserService userService;

    @Autowired
    private final UserRepository userRepository;

    public HighscoreService(@Qualifier("highscoreRepository") HighscoreRepository highscoreRepository,
    @Qualifier("userRepository") UserRepository userRepository) 
    {
        this.highscoreRepository = highscoreRepository;
        this.userRepository = userRepository;
        this.userService = new UserService(userRepository);
    }

    public List<Highscore> getHighscores() 
    {
        // TODO: This is for testing purposes only:
        Highscore testHighscore1=new Highscore();
        testHighscore1.setCreated(LocalDateTime.now());
        testHighscore1.setScore(4242);
        testHighscore1.setUserid(1L);
        createHighscore(testHighscore1);

        return this.highscoreRepository.findAll();
    }

    public Highscore createHighscore(Highscore newHighscore) 
    {
        // saves the given entity but data is only persisted in the database once
        // flush() is called
        newHighscore = highscoreRepository.save(newHighscore);
        highscoreRepository.flush();
        log.debug("Created Information for Highscore: {}", newHighscore);
        return newHighscore;
    }

}

package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LobbyService {

    private final Logger log = LoggerFactory.getLogger(LobbyService.class);

    private final LobbyRepository lobbyRepository;

    @Autowired
    public LobbyService(@Qualifier("lobbyRepository") LobbyRepository lobbyRepository) {
        this.lobbyRepository = lobbyRepository;
    }

    public List<Lobby> getLobbies() {
        return this.lobbyRepository.findAll();
    }

    public Lobby getLobby(Long id) {
        return checkIfLobbyExistsById(id);
    }

    public Lobby createLobby(Lobby newLobby) {
        newLobby.setNumberOfPlayers(1);
        newLobby.setGameStarted(false);
        checkIfUserAlreadyInLobby(newLobby);
        newLobby = lobbyRepository.save(newLobby);
        lobbyRepository.flush();

        log.debug("Created Information for Lobby: {}", newLobby);
        return newLobby;
    }

    /**
     * This is a helper method that will check whether the user,
     * which wants to create/join a lobby, is already in a lobby
     * or not. The method will do nothing if the user is not in
     * a lobby yet and throw an error otherwise.
     *
     * @param lobbyToBeCreated
     * @throws org.springframework.web.server.ResponseStatusException
     * @see Lobby
     */

     private Lobby checkIfUserAlreadyInLobby(Lobby lobbyToBeCreated) {
         Long UserId = lobbyToBeCreated.getUsersInLobby().get(0);
         Optional<Lobby> lobby = lobbyRepository.findLobbyByUserId(UserId);
         if (lobby.isPresent()){
             String errorMessage = "User can only be in one Lobby";
             throw new ResponseStatusException(HttpStatus.CONFLICT, errorMessage);
         }
         return lobbyToBeCreated;
     }

    /**
     * This is a helper method that will check whether the user,
     * which wants to create/join a lobby, is already in a lobby
     * or not. The method will do nothing if the user is not in
     * a lobby yet and throw an error otherwise.
     *
     * @param id
     * @throws org.springframework.web.server.ResponseStatusException
     * @see Lobby
     */

     private Lobby checkIfLobbyExistsById(Long id) {
         Optional<Lobby> foundLobby = lobbyRepository.findById(id);
         return foundLobby.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                 String.format("Lobby with Lobby-ID %d does not exist!", id)));
     }

}

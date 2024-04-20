package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.User;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LobbyService {

    private final Logger log = LoggerFactory.getLogger(LobbyService.class);

    @Autowired
    private final LobbyRepository lobbyRepository;

    @Autowired
    private final UserService userService;

    @Autowired
    private final UserRepository userRepository;

    public LobbyService(@Qualifier("lobbyRepository") LobbyRepository lobbyRepository,
                        @Qualifier("userRepository") UserRepository userRepository) {
        this.lobbyRepository = lobbyRepository;
        this.userRepository = userRepository;
        this.userService = new UserService(userRepository);
    }

    public List<Lobby> getLobbies() {
        return this.lobbyRepository.findAll();
    }

    public Lobby getLobby(Long id) {
        return checkIfLobbyExistsById(id);
    }

    public Lobby createLobby(Lobby newLobby) {
        //if there is no lobby, create one... if there already is a lobby, add player to lobby
        //after M3 delete if/else statement
        if (getLobbies().isEmpty()){
            Long userId = newLobby.getUsersInLobby().get(0);
            User foundUser = checkIfPlayerExists(userId);
            checkIfUserAlreadyInLobby(userId);
            newLobby.setNumberOfPlayers(1);
            newLobby.setGameStarted(false);
            List<User> players = new ArrayList<>();
            players.add(foundUser);
            newLobby.setPlayers(players);
            newLobby = lobbyRepository.save(newLobby);
            lobbyRepository.flush();
            log.debug("Created Information for Lobby: {}", newLobby);
            return newLobby;
        }
        Lobby lobby = getLobbies().get(0);
        return addPlayertoLobby(lobby.getId(), newLobby.getUsersInLobby().get(0));
    }

    public Lobby addPlayertoLobby(Long lobbyId, Long userId) {
        User foundUser = checkIfPlayerExists(userId);
        Lobby lobby = checkIfLobbyExistsById(lobbyId);
        checkIfUserAlreadyInLobby(userId);
        if (lobby.addPlayer(foundUser, lobby)) {
            return lobby;
        }
        String errorMessage = "Lobby is already full!";
        throw new ResponseStatusException(HttpStatus.CONFLICT, errorMessage);
    }

    public void removePlayerFromLobby(Long lobbyId, Long userId) {
        User foundUser = checkIfPlayerExists(userId);
        Lobby lobby = checkIfLobbyExistsById(lobbyId);
        checkIfPlayerInLobby(lobby, userId);
        lobby.removePlayer(foundUser);
        if (lobby.getNumberOfPlayers() == 0) {
            lobbyRepository.delete(lobby);
            lobbyRepository.flush();
        }
    }

    /**
     * This is a helper method that will check whether the user,
     * which wants to create/join a lobby, is already in a lobby
     * or not. The method will do nothing if the user is not in
     * a lobby yet and throw an error otherwise.
     *
     * @param userId
     * @throws org.springframework.web.server.ResponseStatusException
     * @see Lobby
     */

     private Optional<Lobby> checkIfUserAlreadyInLobby(Long userId) {
         Optional<Lobby> lobby = lobbyRepository.findLobbyByUserId(userId);
         if (lobby.isPresent()){
             String errorMessage = "User can only be in one Lobby";
             throw new ResponseStatusException(HttpStatus.CONFLICT, errorMessage);
         }
         return lobby;
     }

    /**
     * This is a helper method that will check whether a lobby
     * with the specified ID exists or not. The method will return
     * found lobby or else throw an error.
     *
     * @param id
     * @throws org.springframework.web.server.ResponseStatusException
     * @see Lobby
     */

    public Lobby checkIfLobbyExistsById(Long id) {
         Optional<Lobby> foundLobby = lobbyRepository.findById(id);
         return foundLobby.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                 String.format("Lobby with Lobby-ID %d does not exist!", id)));
     }

    /**
     * This is a helper method that will check whether the user,
     * which wants to withdraw from a lobby, is indeed in the
     * specified lobby. If the user is in the specified lobby,
     * the truth value true is returned, otherwise an error
     * message is shown.
     *
     * @param lobby
     * @param userId
     * @throws org.springframework.web.server.ResponseStatusException
     * @see Lobby
     */

    private boolean checkIfPlayerInLobby(Lobby lobby, Long userId) {
        List<Long> playersInLobby = lobby.getUsersInLobby();
        if (playersInLobby.contains(userId)) {
            return true;
        }
        String errorMessage = "User not found in specified Lobby";
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage);
    }

    /**
     * This is a helper method that will check whether the user,
     * which wants to withdraw from a lobby, is indeed in the
     * specified lobby. If the user is in the specified lobby,
     * the truth value true is returned, otherwise an error
     * message is shown.
     *
     * @param userId
     * @throws org.springframework.web.server.ResponseStatusException
     * @see ch.uzh.ifi.hase.soprafs24.entity.User
     */

    private User checkIfPlayerExists(Long userId) {
        return userService.getUser(userId);

    }

}

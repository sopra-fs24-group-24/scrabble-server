package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.repository.HandRepository;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.repository.ScoreRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserSlimGetDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@Transactional
public class LobbyService {

    private final Logger log = LoggerFactory.getLogger(LobbyService.class);

    @Autowired
    private final LobbyRepository lobbyRepository;

    @Autowired
    private final HandRepository handRepository;

    @Autowired
    private final ScoreRepository scoreRepository;

    @Autowired
    private final UserService userService;

    @Autowired
    private final UserRepository userRepository;

    public LobbyService(@Qualifier("lobbyRepository") LobbyRepository lobbyRepository,
                        @Qualifier("userRepository") UserRepository userRepository,
                        @Qualifier("handRepository") HandRepository handRepository,
                        @Qualifier("scoreRepository") ScoreRepository scoreRepository) {
        this.lobbyRepository = lobbyRepository;
        this.userRepository = userRepository;
        this.handRepository = handRepository;
        this.scoreRepository = scoreRepository;
        this.userService = new UserService(userRepository);
    }

    public List<Lobby> getLobbies() {
        return this.lobbyRepository.findAll();
    }

    public Lobby getLobby(Long id) {
        return checkIfLobbyExistsById(id);
    }

    public Lobby createLobby(Lobby newLobby) {
        Long userId = newLobby.getUsersInLobby().get(0);
        User foundUser = checkIfPlayerExists(userId);
        checkIfUserAlreadyInLobby(userId);
        newLobby.setNumberOfPlayers(1);
        newLobby.setGameStarted(false);
        List<User> players = new ArrayList<>();
        players.add(foundUser);
        newLobby.setPlayers(players);
        if (newLobby.getIsPrivate()) {
            newLobby.setPin(createUniquePin());
        }
        newLobby = lobbyRepository.save(newLobby);
        lobbyRepository.flush();
        return newLobby;
    }

    public Lobby addPlayertoPrivateLobby(int lobbyPin,Long userId)
    {
        Lobby lobby=checkIfLobbyExistsByPin(lobbyPin);
        return addPlayertoLobby(lobby.getId(), userId);
    }

    public void transformUsersIntoUsersSlim(LobbyGetDTO lobbyGetDTO, Lobby lobby){
        List<UserSlimGetDTO> playersSlim = new ArrayList<>();
        for (User player : lobby.getPlayers()){
            UserSlimGetDTO newPlayer = new UserSlimGetDTO();
            newPlayer.setId(player.getId());
            newPlayer.setUsername(player.getUsername());
            newPlayer.setStatus(player.getStatus());
            playersSlim.add(newPlayer);
        }
        lobbyGetDTO.setPlayers(playersSlim);
    }

    public void removeHandsFromOtherPlayers(LobbyGetDTO lobbyGetDTO, Long userId){
        List<Hand> hand = new ArrayList<>();
        if (lobbyGetDTO.getGameStarted()){
            for (Hand currentHand : lobbyGetDTO.getGameOfLobby().getHands()){
                if (Objects.equals(currentHand.getHanduserid(), userId)){
                    hand.add(currentHand);
                }
            }
            if (hand.size() != 1){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You can only have access to your own hand");
            }
            lobbyGetDTO.getGameOfLobby().setHands(hand);
        }

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

    public void deleteLobby(Long lobbyId)
    {
        Lobby lobby = checkIfLobbyExistsById(lobbyId);
        List<User> players=lobby.getPlayers();

        for (User user : players) 
        {
            removePlayerFromLobby(lobbyId, user.getId());    
        }
    }

    public void removePlayerFromLobby(Long lobbyId, Long userId) {
        User foundUser = checkIfPlayerExists(userId);
        Lobby lobby = checkIfLobbyExistsById(lobbyId);
        checkIfPlayerInLobby(lobby, userId);
        lobby.removePlayer(foundUser);

        Game game=lobby.getGameOfLobby();

        if(game!=null)
        {
            game = removePlayer(game, foundUser);
            if (lobby.getNumberOfPlayers() == 1){
                game.setGameOver(true);
            }
        }
        if (lobby.getNumberOfPlayers() == 0) 
        {
            if(game!=null)
                game.getBag().getTiles().clear();

            lobbyRepository.delete(lobby);
            lobbyRepository.flush();
        }
    }

    public Game removePlayer(Game game, User user){
        if(game.getCurrentPlayer()==user.getId())
        {
            User nextPlayer = game.getNextPlayer();
            game.setCurrentPlayer(nextPlayer.getId());
        }

        int index = 0;
        Long idHand = 0L;
        for (Hand hand : game.getHands()) {
            if (Objects.equals(hand.getHanduserid(), user.getId())){
                game.getBag().putTilesInBag(hand.getHandtiles());
                hand.removeTilesFromHand(hand.getHandtiles());
                idHand = hand.getId();
                break;
            }
            index++;
        }
        game.getHands().remove(index);
        handRepository.delete(handRepository.findById(idHand).orElseThrow());
        handRepository.flush();

        index = 0;
        Long idScore = 0L;
        for (Score score : game.getScores()){
            if (Objects.equals(score.getScoreUserId(), user.getId())){
                idScore = score.getId();
                break;
            }
            index++;
        }
        game.getScores().remove(index);
        scoreRepository.delete(scoreRepository.findById(idScore).orElseThrow());
        scoreRepository.flush();

        index = 0;
        for (User player : game.getPlayers()){
            if (Objects.equals(player.getId(), user.getId())){
                break;
            }
            index++;
        }
        game.getPlayers().remove(index);

        return game;
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

    public Lobby checkIfLobbyExistsByPin(int pin)
    {
        Optional<Lobby> foundLobby = lobbyRepository.findLobbyByPin(pin);
        return foundLobby.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("Private lobby with PIN %d does not exist!", pin)));
    }

    private int createUniquePin() {
        boolean isUniquePin;
        int pin;

        do {
            isUniquePin = true;
            Random rand = new Random();
            pin = rand.nextInt(100000, 999999);

            for (Lobby lobby : lobbyRepository.findAll()) {
                if (lobby.getPin() == pin) {
                    isUniquePin = false;
                }
            }
        } while (!isUniquePin);

        return pin;
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

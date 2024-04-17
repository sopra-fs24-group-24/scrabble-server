package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Tile;
import ch.uzh.ifi.hase.soprafs24.entity.User;
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

import javax.persistence.criteria.CriteriaBuilder;
import java.util.*;

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

    public void skipTurn(User user, Long gameId) {
        authenticateUserForMove(user, gameId);
        makeNextPlayerToCurrentPlayer(gameId);
    }

    public void makeNextPlayerToCurrentPlayer(Long gameId) {
        Optional<Game> game = gameRepository.findById(gameId);

        if (game.isPresent()) {
            User nextPlayer = game.get().getNextPlayer();
            game.get().setCurrentPlayer(nextPlayer.getId());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("The game with id %s does not exist!", gameId));
        }
    }

    public void placeTilesOnBoard(Game game) {
        Game foundGame = checkIfGameExists(game);
        List<Tile> updatedPlayfield = game.getPlayfield();
        List<Tile> persistedPlayfield = foundGame.getPlayfield();

        validMove(updatedPlayfield, persistedPlayfield);

        // update playfield and save it in database
        foundGame.setPlayfield(updatedPlayfield);
        gameRepository.flush();
    }

    public void validMove(List<Tile> updatedPlayfield, List<Tile> persistedPlayfield) {
        // save the indices of the new tiles in a list
        List<Integer> updatedIndices = new ArrayList<Integer>();
        // search for the new tiles and add them to the list
        for (int index = 0; index < 255; index++) {
            if (updatedPlayfield.get(index) != persistedPlayfield.get(index)) {
                updatedIndices.add(index);
            }
        }

        int sizeOfUpdatedIndices = updatedIndices.size();
        // sort array with indices in ascending order
        updatedIndices.sort(Comparator.naturalOrder());

        // check if new tiles are placed on board
        if (sizeOfUpdatedIndices == 0){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You need to place at least 1 Tile");
        }

        // check if valid move

        // first word is placed on playfield
        if (persistedPlayfield.stream().allMatch(Objects::isNull)) {
            // since first word needs to be placed across middle of the board, the arraylist needs to include index 112
            if (!updatedIndices.contains(112) || sizeOfUpdatedIndices == 1) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "First word needs to contain at least 2 letters and has to be placed on the center of the board");
            }
            else {
                int firstElement = updatedIndices.get(0);
                // word is placed horizontally
                if (updatedIndices.get(0) / 15 == updatedIndices.get(sizeOfUpdatedIndices - 1) / 15) {

                    // check if all newly placed tiles are on the same row and connected with each other
                    for (int j = 1; j < sizeOfUpdatedIndices; j++) {
                        int index = firstElement + j;
                        if (!updatedIndices.contains(index)) {
                            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                    "You either have to place a word vertically or horizontally");
                        }
                    }
                }
            }
        }

        else if (sizeOfUpdatedIndices > 1) {

            boolean newWordConnectedToExistingTile = false;
            int firstElement = updatedIndices.get(0);
            int lastElement = updatedIndices.get(sizeOfUpdatedIndices - 1);

            // check whether word is placed vertically or horizontally
            // word is placed horizontally
            if (firstElement / 15 == lastElement / 15) {

                // check if all newly placed tiles are on the same row and connected with each other or with an existing tile
                for (int j = 1; j < sizeOfUpdatedIndices; j++) {
                    int index = firstElement + j;
                    if (updatedIndices.contains(index) && persistedPlayfield.get(index) == null) {
                        continue;
                    }
                    else if (persistedPlayfield.get(index) instanceof Tile && !updatedIndices.contains(index)) {
                        newWordConnectedToExistingTile = true;
                    }
                    else {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "You either have to place a word vertically or horizontally");
                    }
                }

                // check if an existing tile is placed in front of the word
                // check if leftmost letter in word is not placed in first column
                if (firstElement % 15 != 0) {
                    if (persistedPlayfield.get(firstElement - 1) instanceof Tile) {
                        newWordConnectedToExistingTile = true;
                    }
                }
                // check if an existing tile is placed after the word
                // check if rightmost letter in word is not placed in last column
                if ((lastElement + 1) % 15 != 0) {
                    if (persistedPlayfield.get(firstElement + 1) instanceof Tile) {
                        newWordConnectedToExistingTile = true;
                    }
                }
            }

            // word is placed vertically
            else {
                // check if all newly placed tiles are on the same column and connected with each other
                for (int j = 1; j < sizeOfUpdatedIndices; j++) {
                    int index = firstElement + 15 * j;
                    if (updatedIndices.contains(index) && persistedPlayfield.get(index) == null) {
                        continue;
                    }
                    else if (persistedPlayfield.get(index) instanceof Tile && !updatedIndices.contains(index)) {
                        newWordConnectedToExistingTile = true;
                    }
                    else {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "You either have to place a word vertically or horizontally");
                    }
                }

                // check if an existing tile is placed above the word
                // check if uppermost letter in word is not placed in first row
                if (firstElement / 15 != 0) {
                    if (persistedPlayfield.get(firstElement - 15) instanceof Tile) {
                        newWordConnectedToExistingTile = true;
                    }
                }

                // check if an existing tile is placed below the word
                // check if lowest letter in word is not placed in last row
                if (firstElement / 15 != 14) {
                    if (persistedPlayfield.get(firstElement + 15) instanceof Tile) {
                        newWordConnectedToExistingTile = true;
                    }
                }
            }

            // check if placed word is connected with an existing tile
            if (!newWordConnectedToExistingTile) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Your placed word needs to be connected with an existing tile");
            }
        }

        // list 'updatedIndices' contains only 1 letter
        else {
            // check where existing tile could be
            boolean newWordConnectedToExistingTile = false;
            int element = updatedIndices.get(0);

            // calculate row and column of element
            int row = element / 15;
            int column = element % 15;

            // store neighbor cells of new tile in an array
            List<Integer> neighborCells = new ArrayList<Integer>();

            // add valid neighbor cells to array
            for (int j = 0; j < 3; j++) {
                if (j % 2 == 0) {
                    if (row - 1 >= 0){
                        neighborCells.add(element - 15);
                    }
                    if (row + 1 <= 14){
                        neighborCells.add(element + 15);
                    }
                }
                else {
                    if (column - 1 >= 0){
                        neighborCells.add(element - 1);
                    }
                    if (column + 1 <= 14){
                        neighborCells.add(element + 1);
                    }
                }
            }

            // check if a neighbor cell contains a tile
            for (int neighborCell : neighborCells){
                if (persistedPlayfield.get(neighborCell) instanceof Tile){
                    newWordConnectedToExistingTile = true;
                }
            }

            if (!newWordConnectedToExistingTile) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Your placed word needs to be connected with an existing tile");
            }

        }

    }

    public void authenticateUserForMove(User userInput, Long gameId) {
        Optional<Game> game = gameRepository.findById(gameId);

        if (game.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The game does not exist!");
        } else if (!game.get().getCurrentPlayer().equals(userInput.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "It's not your turn!");
        }
    }

    /**
     * This is a helper method that will check whether a game
     * with the specified ID exists or not. The method will return
     * found game or else throw an error.
     *
     * @param game
     * @throws org.springframework.web.server.ResponseStatusException
     * @see Game
     */
    private Game checkIfGameExists(Game game) {
        return gameRepository.findById(game.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Indicated Game not found!"));
    }


    /**
     * This is a helper method that will check whether a board
     * has the correct size. The method will return nothing
     * or else throw an error.
     *
     * @param board
     * @throws org.springframework.web.server.ResponseStatusException
     * @see Tile
     */
    private void checkIfBoardValid(List<Tile> board) {
        // check if board size correct
        if (board.size() != 225) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Board Dimensions need to be 15x15");
        }
    }
}

package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Tile;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameDTOMapperTest {
    @Test
    public void testCreateGame_fromGamePostDTO_toGame_success() {
        // create GamePostDTO
        GamePostDTO gamePostDTO = new GamePostDTO();
        gamePostDTO.setId(1L);
        gamePostDTO.setCurrentPlayer(1L);
        List<Tile> playfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            playfield.add(null);
        }
        Tile tile1 = new Tile('C', 3);
        tile1.setBoardidx(112);
        Tile tile2 = new Tile('A', 3);
        tile2.setBoardidx(113);
        Tile tile3 = new Tile('T', 5);
        tile3.setBoardidx(114);

        playfield.set(112, tile1);
        playfield.set(113, tile2);
        playfield.set(114, tile3);
        gamePostDTO.setPlayfield(playfield);

        // MAP -> Create Game
        Game game = GameDTOMapper.INSTANCE.convertGamePostDTOToEntity(gamePostDTO);

        // check content
        assertEquals(gamePostDTO.getId(), game.getId());
        assertEquals(gamePostDTO.getCurrentPlayer(), game.getCurrentPlayer());
        assertEquals(gamePostDTO.getPlayfield(), game.getPlayfield());
    }
}

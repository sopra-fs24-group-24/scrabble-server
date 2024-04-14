package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameGetDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GameDTOMapper {

    GameDTOMapper INSTANCE = Mappers.getMapper(GameDTOMapper.class);


    @Mapping(source = "id", target = "id")
    @Mapping(source = "currentPlayer", target = "currentPlayer")
    @Mapping(source = "playfield", target = "playfield")
    @Mapping(source = "scores", target = "scores")
    @Mapping(source = "hands", target = "hands")
    @Mapping(source= "players", target="players")
    GameGetDTO convertEntityToGameGetDTO(Game game);
}

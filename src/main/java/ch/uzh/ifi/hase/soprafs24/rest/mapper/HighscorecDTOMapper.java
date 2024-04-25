package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Highscore;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.HighscoreGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.HighscorePostDTO;

import java.time.LocalDateTime;

import javax.persistence.Column;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface HighscorecDTOMapper 
{

    HighscorecDTOMapper INSTANCE = Mappers.getMapper(HighscorecDTOMapper.class);

    @Mapping(source = "score", target = "score")
    @Mapping(source = "userid", target = "userid")
    @Mapping(source = "created", target = "created")

    HighscoreGetDTO convertEntityToHighscoreGetDTO(Highscore highscore);

    @Mapping(source = "score", target = "score")
    @Mapping(source = "userid", target = "userid")
    @Mapping(source = "created", target = "created")
    Highscore convertHighscorePostDTOToEntity(HighscorePostDTO highscorePostDTOPostDTO);
}

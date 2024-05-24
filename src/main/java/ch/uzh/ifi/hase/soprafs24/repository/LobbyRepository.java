package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("lobbyRepository")
public interface LobbyRepository extends JpaRepository<Lobby, Long> {
   @Query("SELECT l FROM Lobby l JOIN l.usersInLobby ul WHERE :UserId IN (ul)")
   Optional<Lobby> findLobbyByUserId(Long UserId);
   Optional<Lobby> findLobbyByPin(String pin);
}

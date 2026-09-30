package com.guessword.guesstheword.repository;

import com.guessword.guesstheword.entity.Game;
import com.guessword.guesstheword.dto.UserReportEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    long countByUserIdAndPlayedDate(Long userId, LocalDate playedDate);
    List<Game> findByUserIdAndPlayedDate(Long userId, LocalDate playedDate);
    long countByPlayedDate(LocalDate playedDate);
    long countByPlayedDateAndWonTrue(LocalDate playedDate);
    long countByUserIdAndPlayedDateAndWonTrue(Long userId, LocalDate playedDate);

    @Query("select new com.guessword.guesstheword.dto.UserReportEntry(g.playedDate, count(distinct g.id), "
            + "coalesce(sum(case when q.correct = true then 1L else 0L end), 0L)) "
            + "from Game g left join Guess q on q.game.id = g.id "
            + "where g.user.id = :userId group by g.playedDate order by g.playedDate asc")
    List<UserReportEntry> findUserReportByPlayedDate(@Param("userId") Long userId);

    @Query("select count(distinct g.user.id) from Game g where g.playedDate = :playedDate")
    long countDistinctUsersByPlayedDate(@Param("playedDate") LocalDate playedDate);
}

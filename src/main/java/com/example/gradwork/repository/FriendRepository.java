package com.example.gradwork.repository;

import com.example.gradwork.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friendship, Long> {

    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.userId1 = :userId1 AND f.userId2 = :userId2) OR " +
            "(f.userId1 = :userId2 AND f.userId2 = :userId1)")
    Optional<Friendship> findFriendship(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2);

    @Query("SELECT f FROM Friendship f WHERE " +
            "f.userId1 = :userId OR f.userId2 = :userId")
    List<Friendship> findFriendshipsByUserId(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friendship f WHERE " +
            "(f.userId1 = :userId1 AND f.userId2 = :userId2) OR " +
            "(f.userId1 = :userId2 AND f.userId2 = :userId1)")
    boolean existsFriendship(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2);
}

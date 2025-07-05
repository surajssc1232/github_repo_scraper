package com.example.demo.repository;

import com.example.demo.entity.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositoryRepository extends JpaRepository<RepositoryEntity, Long> {
    @Query("SELECT r FROM RepositoryEntity r WHERE (:language IS NULL OR r.language = :language) AND (:minStars IS NULL OR r.stars >= :minStars) AND (:minForks IS NULL OR r.forks >= :minForks)")
    List<RepositoryEntity> findByLanguageAndMinStarsAndMinForks(@Param("language") String language, @Param("minStars") Integer minStars, @Param("minForks") Integer minForks);
} 
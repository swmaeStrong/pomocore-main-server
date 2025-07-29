package com.swmStrong.demo.domain.group.repository;

import com.swmStrong.demo.domain.group.entity.Group;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query(value = "SELECT * FROM \"group\" WHERE tags && CAST(:tags AS text[])", nativeQuery = true)
    List<Group> findByTagsContaining(@Param("tags") List<String> tags);

    @Query(value = "SELECT * FROM \"group\" WHERE tags @> CASE(:tags AS text[])", nativeQuery = true)
    List<Group> findByAllTagsContaining(@Param("tags") List<String> tags);

    boolean existsByName(String name);
}

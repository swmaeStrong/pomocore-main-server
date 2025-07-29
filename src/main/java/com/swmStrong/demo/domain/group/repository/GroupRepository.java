package com.swmStrong.demo.domain.group.repository;

import com.swmStrong.demo.domain.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}

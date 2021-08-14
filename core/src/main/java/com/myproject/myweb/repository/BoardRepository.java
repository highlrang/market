package com.myproject.myweb.repository;

import com.myproject.myweb.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
}

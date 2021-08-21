package com.myproject.myweb.repository;

import com.myproject.myweb.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

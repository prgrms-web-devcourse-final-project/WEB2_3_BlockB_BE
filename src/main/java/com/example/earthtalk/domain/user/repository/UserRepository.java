package com.example.earthtalk.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.earthtalk.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}

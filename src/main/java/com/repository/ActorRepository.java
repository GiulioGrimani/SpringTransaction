package com.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.entity.Actor;

public interface ActorRepository extends JpaRepository<Actor, Integer> {

}

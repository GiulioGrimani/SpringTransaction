package com.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.entity.Film;

public interface FilmRepository extends JpaRepository<Film, Integer> {

}

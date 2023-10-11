package com.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.dto.FilmDTO;

public interface FilmService {

	public ResponseEntity<FilmDTO> insertOrUpdateFilm(FilmDTO filmDTO);

	public ResponseEntity<List<FilmDTO>> getFilms();

}

package com.service;

import org.springframework.http.ResponseEntity;

import com.dto.ActorFilmsDTO;
import com.dto.FilmActorsDTO;

public interface FilmActorService {

	public ResponseEntity<FilmActorsDTO> insertFilmWithActors(FilmActorsDTO filmActorsDTO);

	public ResponseEntity<ActorFilmsDTO> insertActorWithFilms(ActorFilmsDTO actorFilmsDTO);

}

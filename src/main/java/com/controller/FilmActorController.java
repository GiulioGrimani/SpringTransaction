package com.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dto.ActorDTO;
import com.dto.ActorFilmsDTO;
import com.dto.FilmActorsDTO;
import com.service.ActorService;
import com.service.FilmActorService;

@RestController
@RequestMapping("/api")
public class FilmActorController {

	@Autowired
	private ActorService actorService;

	@Autowired
	private FilmActorService filmActorService;

	@PostMapping("/insertActor")
	public ResponseEntity<ActorDTO> insertActor(@RequestBody ActorDTO actorDTO) {
		return actorService.insertOrUpdateActor(actorDTO);
	}

	@PutMapping("/updateActor")
	public ResponseEntity<ActorDTO> updateCompany(@RequestBody ActorDTO actorDTO) {
		return actorService.insertOrUpdateActor(actorDTO);
	}

	@PostMapping("/insertFilmActors")
	public ResponseEntity<FilmActorsDTO> insertFilmActors(@RequestBody FilmActorsDTO filmActorsDTO) {
		return filmActorService.insertFilmWithActors(filmActorsDTO);
	}

	@PostMapping("/insertActorFilms")
	public ResponseEntity<ActorFilmsDTO> insertActorFilms(@RequestBody ActorFilmsDTO actorFilmsDTO) {
		return filmActorService.insertActorWithFilms(actorFilmsDTO);
	}

	@PostMapping("/doTransaction1")
	public ResponseEntity<List<ActorDTO>> doTransaction1(@RequestBody ActorDTO actorDTO) {
		return actorService.doTransaction1(actorDTO);
	}

}

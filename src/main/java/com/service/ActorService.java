package com.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.dto.ActorDTO;

public interface ActorService {

	public ResponseEntity<ActorDTO> insertOrUpdateActor(ActorDTO actorDTO);

	public ResponseEntity<List<ActorDTO>> getActors();

	public ResponseEntity<List<ActorDTO>> doTransaction1(ActorDTO actorDTO);

}

package com.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dto.ActorDTO;
import com.entity.Actor;
import com.repository.ActorRepository;

@Service
@Transactional
public class ActorServiceImpl implements ActorService {

	@Autowired
	private ActorRepository actorRepository;

	@Override
	public ResponseEntity<ActorDTO> insertOrUpdateActor(ActorDTO actorDTO) {
		Actor toInsert = mapActorDTOToEntity(actorDTO);
		Actor toReturn = actorRepository.save(toInsert);
		ActorDTO result = mapActorToDTO(toReturn);
		return new ResponseEntity<ActorDTO>(result, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<ActorDTO>> getActors() {
		List<Actor> toReturn = actorRepository.findAll();
		List<ActorDTO> result = new ArrayList<>();
		for (Actor a : toReturn) {
			result.add(mapActorToDTO(a));
		}
		return new ResponseEntity<List<ActorDTO>>(result, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<ActorDTO>> doTransaction1(ActorDTO actorDTO) {
		ActorDTO myActorDTO = insertOrUpdateActor(actorDTO).getBody();
		// Modifico, giusto per testare, il cognome
		myActorDTO.setLastName("Cerqua");
		insertOrUpdateActor(myActorDTO);
		return getActors();
	}

	private Actor mapActorDTOToEntity(ActorDTO actorDTO) {
		Actor actor = new Actor();
		actor.setActorId(actorDTO.getActorId());
		actor.setFirstName(actorDTO.getFirstName());
		actor.setLastName(actorDTO.getLastName());
		return actor;
	}

	private ActorDTO mapActorToDTO(Actor actor) {
		ActorDTO actorDTO = new ActorDTO();
		actorDTO.setActorId(actor.getActorId());
		actorDTO.setFirstName(actor.getFirstName());
		actorDTO.setLastName(actor.getLastName());
		return actorDTO;
	}

}

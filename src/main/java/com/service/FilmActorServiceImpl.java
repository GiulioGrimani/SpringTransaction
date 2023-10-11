package com.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dto.ActorDTO;
import com.dto.ActorFilmsDTO;
import com.dto.FilmActorsDTO;
import com.dto.FilmDTO;
import com.entity.Actor;
import com.entity.Film;
import com.repository.ActorRepository;
import com.repository.FilmRepository;

@Service
@Transactional
public class FilmActorServiceImpl implements FilmActorService {

	@Autowired
	private FilmRepository filmRepository;

	@Autowired
	private ActorRepository actorRepository;

	/*
	 * Siccome Film e' l'owner della relazione, mi basta inserire gli Actor nella
	 * lista di Actor del Film e inserire/aggiornare il Film. Controllero' inoltre
	 * se ciascun Actor e' da inserire o da aggiornare
	 */
	@Override
	public ResponseEntity<FilmActorsDTO> insertFilmWithActors(FilmActorsDTO filmActorsDTO) {

		// Processo la lista di ActorDTO: se e' da aggiornare recupero l'Actor dal DB,
		// lo modifico e lo inserisco nella lista da inserire/aggiornare. Se e'
		// da inserire, lo mappo ad Actor e lo inserisco nella medesima lista.
		List<Actor> actors = new ArrayList<>();
		for (ActorDTO a : filmActorsDTO.getActorsDTO()) {
			if (a.getActorId() != null) {
				Actor tempActor = actorRepository.findById(a.getActorId()).get();
				if (a.getFirstName() != null) {
					tempActor.setFirstName(a.getFirstName());
				}
				if (a.getLastName() != null) {
					tempActor.setLastName(a.getLastName());
				}
				actors.add(tempActor);

			} else {
				actors.add(mapActorDTOToEntity(a));
			}
		}

		// Controllo se il film e' nuovo (da inserire nel DB) o no (da recuperare dal DB
		// per poi aggiornarlo)
		Integer filmId = filmActorsDTO.getFilmDTO().getFilmId();
		Film film = filmId == null ? mapFilmDTOToEntity(filmActorsDTO.getFilmDTO())
				: filmRepository.findById(filmId).get();

		// E' stato recuperato dal DB, quindi va aggiornato
		if (filmId != null && filmActorsDTO.getFilmDTO().getTitle() != null) {
			film.setTitle(filmActorsDTO.getFilmDTO().getTitle());
		}

		// Imposto al Film la sua lista di Actor aggiornandola (aggiungendo i nuovi
		// Actor alla lista e sostituendo quelli gia' inseriti per aggiornarli)
		for (Actor a : actors) {
			Integer actorId = a.getActorId();
			// Se c'e' l'id, significa che lo devo aggiornare, quindi
			// rimuovo quello che trovo e ci metto quello aggiornato
			// (altrimenti nella junction table cercherei di inserire un record con una PK
			// gia' presente)
			if (actorId != null) {
				for (Actor actorToUpdate : film.getActors()) {
					if (actorToUpdate.getActorId() == actorId) {
						film.getActors().remove(actorToUpdate);
						break;
					}
				}
			}
			film.getActors().add(a);
		}

		// Faccio il save del Film, quindi inserisce chi deve inserire ed aggiorna chi
		// deve aggiornare
		Film createdFilm = filmRepository.save(film);

		// Costruisco il DTO che devo restituire
		List<Actor> createdFilmActors = createdFilm.getActors();
		List<ActorDTO> createdFilmActorsDTO = new ArrayList<>();
		for (Actor a : createdFilmActors) {
			createdFilmActorsDTO.add(mapActorToDTO(a));
		}

		FilmActorsDTO result = new FilmActorsDTO();
		result.setFilmDTO(mapFilmToDTO(createdFilm));
		result.setActorsDTO(createdFilmActorsDTO);

		return new ResponseEntity<FilmActorsDTO>(result, HttpStatus.OK);
	}

	/*
	 * Siccome Film e' l'owner della relazione, qui devo inserire l'Actor nella
	 * lista di Actor di ciascun Film e poi aggiornare/inserire tali Film
	 */
	@Override
	public ResponseEntity<ActorFilmsDTO> insertActorWithFilms(ActorFilmsDTO actorFilmsDTO) {

		// Processo la lista di FilmDTO per creare la lista di Film: vedo quale FilmDTO
		// corrisponde ad un Film da inserire e quale ad un Film da aggiornare
		List<Film> films = new ArrayList<>();
		for (FilmDTO f : actorFilmsDTO.getFilmsDTO()) {
			Integer filmId = f.getFilmId();
			if (filmId != null) {
				Film tempFilm = filmRepository.findById(filmId).get();
				if (f.getTitle() != null) {
					tempFilm.setTitle(f.getTitle());
				}
				films.add(tempFilm);
			} else {
				films.add(mapFilmDTOToEntity(f));
			}
		}

		// Vedo se l'ActorDTO corrisponde a un Actor da inserire o da aggiornare, ed
		// aggiungo tale Actor alla lista di Actor di tutti i Film
		Integer actorId = actorFilmsDTO.getActorDTO().getActorId();
		Actor actor = actorId == null ? mapActorDTOToEntity(actorFilmsDTO.getActorDTO())
				: actorRepository.findById(actorId).get();

		// E' stato recuperato dal DB, quindi va aggiornato
		if (actorId != null) {
			String firstName = actorFilmsDTO.getActorDTO().getFirstName();
			String lastName = actorFilmsDTO.getActorDTO().getLastName();
			if (firstName != null) {
				actor.setFirstName(firstName);
			}
			if (lastName != null) {
				actor.setLastName(lastName);
			}
			// Contestualmente, lo rimuovo dalla lista di Actor di ciascun Film,
			// (altrimenti quando lo andro' ad aggiungere alle varie liste di Actor,
			// di ciascun film, nella junction table cercherei di inserire un record con una
			// PK gia' presente)
			for (Film f : films) {
				for (Actor a : f.getActors()) {
					System.out.println(a.getActorId() + " = " + actorId);
					if (a.getActorId() == actorId) {
						f.getActors().remove(a);
						break;
					}
				}
			}
		}

		// Inserisco/aggiorno l'actor
		actorRepository.save(actor);

		// Aggiungo tale Actor alla lista di Actor di ciascun Film per poi
		// fare il save (insert/update) di ciascun Film. Costruisco inoltre il DTO
		// da restituire aggiungendo ad una lista di FilmDTO il mapping di tale Film
		List<FilmDTO> processedFilm = new ArrayList<>();

		for (Film f : films) {
			f.getActors().add(actor);
			Film currentFilm = filmRepository.save(f);
			FilmDTO currentFilmDTO = mapFilmToDTO(currentFilm);
			processedFilm.add(currentFilmDTO);
		}

		// Costruisco il DTO che devo restituire
		ActorFilmsDTO result = new ActorFilmsDTO(mapActorToDTO(actor), processedFilm);
		return new ResponseEntity<ActorFilmsDTO>(result, HttpStatus.OK);
	}

	private Film mapFilmDTOToEntity(FilmDTO filmDTO) {
		Film film = new Film();
		film.setFilmId(filmDTO.getFilmId());
		film.setTitle(filmDTO.getTitle());
		return film;
	}

	private Actor mapActorDTOToEntity(ActorDTO actorDTO) {
		Actor actor = new Actor();
		actor.setActorId(actorDTO.getActorId());
		actor.setFirstName(actorDTO.getFirstName());
		actor.setLastName(actorDTO.getLastName());
		return actor;
	}

	private FilmDTO mapFilmToDTO(Film film) {
		FilmDTO filmDTO = new FilmDTO();
		filmDTO.setFilmId(film.getFilmId());
		filmDTO.setTitle(film.getTitle());
		return filmDTO;
	}

	private ActorDTO mapActorToDTO(Actor actor) {
		ActorDTO actorDTO = new ActorDTO();
		actorDTO.setActorId(actor.getActorId());
		actorDTO.setFirstName(actor.getFirstName());
		actorDTO.setLastName(actor.getLastName());
		return actorDTO;
	}

}

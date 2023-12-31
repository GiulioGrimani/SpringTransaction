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
	 * Questo metodo prende in input un DTO fatto da un FilmDTO e da una lista di
	 * ActorDTO associati al FilmDTO, e li inserisce e/o aggiorna sul DB.
	 * 
	 * Restituisce una ResponseEntity di un DTO composto da un FilmDTO e dalla lista
	 * di tutti gli ActorDTO associati.
	 * 
	 * Siccome Film e' l'owner della relazione, basta inserire gli Actor nella lista
	 * di Actor del Film e inserire/aggiornare il Film. Questo significa che
	 * verranno inseriti e/o aggiornati anche gli Actor associati al Film.
	 */
	@Override
	public ResponseEntity<FilmActorsDTO> insertFilmWithActors(FilmActorsDTO filmActorsDTO) {

		// Processo la lista di ActorDTO: se e' da aggiornare recupero l'Actor dal DB,
		// lo modifico e lo inserisco nella lista da inserire/aggiornare. Se e'
		// da inserire, lo mappo ad Actor e lo inserisco nella medesima lista.
		List<Actor> actors = new ArrayList<>();
		for (ActorDTO a : filmActorsDTO.getActorsDTO()) {
			if (a.getActorId() != null) {
				Actor currentActor = actorRepository.findById(a.getActorId()).get();
				if (a.getFirstName() != null) {
					currentActor.setFirstName(a.getFirstName());
				}
				if (a.getLastName() != null) {
					currentActor.setLastName(a.getLastName());
				}
				actors.add(currentActor);

			} else {
				actors.add(mapActorDTOToEntity(a));
			}
		}

		// Controllo se il FilmDTO corrisponde ad un Film nuovo da inserire nel DB
		// o gia' esistente e quindi da aggiornare
		Integer filmId = filmActorsDTO.getFilmDTO().getFilmId();
		Film film = filmId == null ? mapFilmDTOToEntity(filmActorsDTO.getFilmDTO())
				: filmRepository.findById(filmId).get();

		// Se e' stato recuperato dal DB, va aggiornato
		if (filmId != null && filmActorsDTO.getFilmDTO().getTitle() != null) {
			film.setTitle(filmActorsDTO.getFilmDTO().getTitle());
		}

		// Siamo pronti per aggiornare la lista di Actor del Film (aggiungendo i nuovi
		// Actor alla lista e sostituendo quelli gia' inseriti per aggiornarli)
		for (Actor a : actors) {
			Integer actorId = a.getActorId();
			// Se l'id e' presente, significa che l'Actor e' da aggiornare:
			// conviene rimuovere tale Actor dalla lista di Actor del Film
			// per poi inserire la sua versione aggiornata. Nota bene: aggiungere
			// l'Actor alla lista di Actor del Film senza controllare se sia gia'
			// presente una sua versione non aggiornata, fara' fallire il programma
			// in quanto si cerchera' di inserire nella junction table un record
			// con una PK gia' presente
			if (actorId != null) {
				for (Actor actorToUpdate : film.getActors()) {
					if (actorToUpdate.getActorId().equals(actorId)) {
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
	 * Questo metodo prende in input un DTO fatto da un ActorDTO e da una lista di
	 * FilmDTO associati all'ActorDTO, e li inserisce e/o aggiorna sul DB.
	 * 
	 * Restituisce una ResponseEntity di un DTO composto da un ActorDTO e dalla
	 * lista di tutti i FilmDTO associati.
	 * 
	 * Siccome Film e' l'owner della relazione, si dovra' inserire l'Actor nella
	 * lista di Actor di ciascun Film e poi inserire e/o aggiornare la lista di
	 * Film.
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

		// Se l'Actor e' stato recuperato dal DB, va aggiornato
		if (actorId != null) {
			String firstName = actorFilmsDTO.getActorDTO().getFirstName();
			String lastName = actorFilmsDTO.getActorDTO().getLastName();
			if (firstName != null) {
				actor.setFirstName(firstName);
			}
			if (lastName != null) {
				actor.setLastName(lastName);
			}
			// Contestualmente, l'Actor va rimosso dalla lista di Actor di ciascun Film,
			// (altrimenti, quando lo si andra' ad aggiungere alle varie liste di ciascun
			// Film e si andranno ad inserire/aggiornare i Film, nella junction table
			// starei cercando di inserire un record con una PK gia' presente ed il
			// programma fallirebbe)
			for (Film f : films) {
				for (Actor a : f.getActors()) {
					if (a.getActorId().equals(actorId)) {
						f.getActors().remove(a);
						break;
					}
				}
			}
		}

		// Conviene ora inserire\aggiornare l'Actor per averne eventualmente l'id
		// e per avere la sua versione aggiornata da inserire nella lista di ciascun
		// Film
		actorRepository.save(actor);

		// Aggiungo tale Actor alla lista di Actor di ciascun Film per poi
		// fare il save (insert/update) di ciascun Film. Costruisco inoltre il DTO
		// da restituire aggiungendo ad una lista di FilmDTO il mapping di tale Film.
		// Per costruire la lista di FilmDTO da restituire, prendo prima tutti i Film
		// gia' associati all'Actor (andandolo a ripescare dal DB), ed a tale lista
		// ci aggiungo quelli che sto inserendo ora
		List<FilmDTO> filmsDTOToReturn = new ArrayList<>();

		// Lista di Film gia' associati all'Actor
		List<Film> actorFilms = actorRepository.findById(actor.getActorId()).get().getFilms();

		// Mapping dei Film gia' associati
		for (Film f : actorFilms) {
			filmsDTOToReturn.add(mapFilmToDTO(f));
		}

		// Se l'id e' null, allora non era gia' associato e va inserito nella lista
		for (Film f : films) {
			boolean flag = f.getFilmId() == null;
			f.getActors().add(actor);
			filmRepository.save(f);
			if (flag) {
				filmsDTOToReturn.add(mapFilmToDTO(f));
			}
		}

		// Costruisco il DTO che devo restituire
		ActorFilmsDTO result = new ActorFilmsDTO(mapActorToDTO(actor), filmsDTOToReturn);
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

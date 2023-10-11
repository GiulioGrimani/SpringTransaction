package com.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dto.FilmDTO;
import com.entity.Film;
import com.repository.FilmRepository;

@Service
@Transactional
public class FilmServiceImpl implements FilmService {

	@Autowired
	private FilmRepository filmRepository;

	@Override
	public ResponseEntity<FilmDTO> insertOrUpdateFilm(FilmDTO filmDTO) {
		Film toInsert = mapFilmDTOToEntity(filmDTO);
		Film toReturn = filmRepository.save(toInsert);
		FilmDTO result = mapFilmToDTO(toReturn);
		return new ResponseEntity<FilmDTO>(result, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<FilmDTO>> getFilms() {
		List<Film> toReturn = filmRepository.findAll();
		List<FilmDTO> result = new ArrayList<>();
		for (Film f : toReturn) {
			result.add(mapFilmToDTO(f));
		}
		return new ResponseEntity<List<FilmDTO>>(result, HttpStatus.OK);
	}

	private Film mapFilmDTOToEntity(FilmDTO filmDTO) {
		Film film = new Film();
		film.setFilmId(filmDTO.getFilmId());
		film.setTitle(filmDTO.getTitle());
		return film;
	}

	private FilmDTO mapFilmToDTO(Film film) {
		FilmDTO filmDTO = new FilmDTO();
		filmDTO.setFilmId(film.getFilmId());
		filmDTO.setTitle(film.getTitle());
		return filmDTO;
	}

}

package com.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class FilmActorsDTO {

	private FilmDTO filmDTO;

	private List<ActorDTO> actorsDTO;

}

package com.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilmDTO {

	private Integer filmId;
	private String title;

	public FilmDTO(String title) {
		this.title = title;
	}

}

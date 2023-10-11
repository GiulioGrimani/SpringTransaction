package com.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ActorDTO {

	private Integer actorId;
	private String firstName;
	private String lastName;

	public ActorDTO(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}

}

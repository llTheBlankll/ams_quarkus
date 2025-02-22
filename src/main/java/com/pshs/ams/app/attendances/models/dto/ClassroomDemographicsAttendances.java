package com.pshs.ams.app.attendances.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ClassroomDemographicsAttendances {

	private long male;
	private long female;
}

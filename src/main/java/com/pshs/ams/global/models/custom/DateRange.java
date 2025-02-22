package com.pshs.ams.global.models.custom;

import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode
public class DateRange {

	@QueryParam("startDate")
	private LocalDate startDate;

	@QueryParam("endDate")
	private LocalDate endDate;
}

package com.pshs.ams.models.dto.custom;

import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.QueryParam;

public class SortRequest {

	@QueryParam("sortBy")
	public String sortBy = "id";

	@QueryParam("sortDirection")
	public Sort.Direction sortDirection = Sort.Direction.Ascending;

	public Sort toSort() {
		return Sort.by(sortBy, sortDirection);
	}
}
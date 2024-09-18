package com.pshs.ams.models.dto.custom;

import jakarta.ws.rs.QueryParam;

public class PageRequest {

	@QueryParam("page")
	public int page = 0;

	@QueryParam("size")
	public int size = 10;
}
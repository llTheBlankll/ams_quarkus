package com.pshs.ams.models.dto.custom;

import io.quarkus.panache.common.Page;
import jakarta.ws.rs.QueryParam;

public class PageRequest {

	@QueryParam("page")
	public int page = 0;

	@QueryParam("size")
	public int size = 10;

	public Page toPage() {
		return Page.of(page, size);
	}
}
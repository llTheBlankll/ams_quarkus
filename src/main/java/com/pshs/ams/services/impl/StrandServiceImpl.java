package com.pshs.ams.services.impl;

import com.pshs.ams.models.entities.Strand;
import com.pshs.ams.models.enums.CodeStatus;
import com.pshs.ams.services.StrandService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class StrandServiceImpl implements StrandService {
	@Override
	public List<Strand> getAllStrand(Sort sort, Page page) {
		return List.of();
	}

	@Override
	public Strand createStrand(Strand strand) {
		return null;
	}

	@Override
	public Strand getStrand(Long id) {
		return null;
	}

	@Override
	public CodeStatus deleteStrand(Long id) {
		return null;
	}

	@Override
	public CodeStatus updateStrand(Strand strand) {
		return null;
	}
}
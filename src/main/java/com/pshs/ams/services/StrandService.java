package com.pshs.ams.services;


import com.pshs.ams.models.entities.Strand;
import com.pshs.ams.models.enums.CodeStatus;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import java.util.List;

public interface StrandService {

	List<Strand> getAllStrand(Sort sort, Page page);

	Strand createStrand(Strand strand);

	Strand getStrand(Long id);

	CodeStatus deleteStrand(Long id);

	CodeStatus updateStrand(Strand strand);
}
package com.pshs.ams.services.interfaces;


import com.pshs.ams.models.entities.Strand;
import com.pshs.ams.models.enums.CodeStatus;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import java.util.List;
import java.util.Optional;

public interface StrandService {

	List<Strand> getAllStrand(Sort sort, Page page);

	CodeStatus createStrand(Strand strand);

	Optional<Strand> getStrand(Long id);

	CodeStatus deleteStrand(Long id);

	CodeStatus updateStrand(Strand strand);
}
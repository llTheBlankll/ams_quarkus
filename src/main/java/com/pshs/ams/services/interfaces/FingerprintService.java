package com.pshs.ams.services.interfaces;

import com.pshs.ams.models.dto.custom.MessageDTO;

import java.util.Optional;

public interface FingerprintService {

	Optional<MessageDTO> enrollFingerprint(Integer fingerprintId);
}

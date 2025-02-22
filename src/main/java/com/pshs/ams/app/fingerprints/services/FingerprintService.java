package com.pshs.ams.app.fingerprints.services;

import com.pshs.ams.global.models.custom.MessageResponse;

import java.util.Optional;

public interface FingerprintService {

	Optional<MessageResponse> enrollFingerprint(Integer fingerprintId);
}

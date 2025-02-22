package com.pshs.ams.app.fingerprints.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pshs.ams.global.models.custom.MessageResponse;
import com.pshs.ams.global.models.custom.RFIDCard;
import com.pshs.ams.app.rfid_credentials.models.entities.RfidCredential;
import com.pshs.ams.app.attendances.models.enums.AttendanceMode;
import com.pshs.ams.app.attendances.services.AttendanceService;
import com.pshs.ams.app.fingerprints.services.FingerprintService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;

@ApplicationScoped
@Log4j2
public class FingerprintServiceImpl implements FingerprintService {

	@Inject
	AttendanceService attendanceService;

	@Override
	public Optional<MessageResponse> enrollFingerprint(Integer fingerprintId) {
		if (fingerprintId <= 0) {
			log.debug("Fingerprint ID is invalid: {}", fingerprintId);
			return Optional.empty();
		}

		Optional<RfidCredential> rfidCredential = RfidCredential.find("fingerprintId", fingerprintId).firstResultOptional();

		RFIDCard rfidCard = new RFIDCard();
		if (rfidCredential.isPresent()) {
			rfidCard.setHashedLrn(rfidCredential.get().getHashedLrn());
			rfidCard.setMode(AttendanceMode.IN);
		}

		try {
			MessageResponse messageResponse = attendanceService.fromWebSocket(rfidCard);
			log.debug("Enrolling fingerprint with ID: {}", fingerprintId);
			return Optional.of(messageResponse);
		} catch (JsonProcessingException e) {
			log.error("Error enrolling fingerprint with ID: {}", fingerprintId, e);
			return Optional.empty();
		}
	}
}

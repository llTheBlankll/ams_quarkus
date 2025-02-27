package com.pshs.ams.app.fingerprints.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pshs.ams.global.models.custom.MessageResponse;
import com.pshs.ams.global.models.custom.RFIDCard;
import com.pshs.ams.app.rfid_credentials.models.entities.StudentCredential;
import com.pshs.ams.app.attendances.models.enums.AttendanceMode;
import com.pshs.ams.app.attendances.services.AttendanceService;
import com.pshs.ams.app.fingerprints.services.FingerprintService;
import com.pshs.ams.global.models.enums.CodeStatus;
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

		Optional<StudentCredential> rfidCredential = StudentCredential.find("fingerprintId", fingerprintId).firstResultOptional();
		RFIDCard rfidCard = new RFIDCard();
		if (rfidCredential.isPresent()) {
			rfidCard.setHashedLrn(rfidCredential.get().getHashedLrn());
			rfidCard.setMode(AttendanceMode.IN);
		}

		try {
			// Check if the RFID card has hashed lrn.
			if (rfidCard.getHashedLrn() == null) {
				log.debug("Fingerprint not found: {}", fingerprintId);
				return Optional.of(new MessageResponse(
					"Fingerprint is not enrolled",
					CodeStatus.NOT_FOUND
				));
			}

			MessageResponse messageResponse = attendanceService.fromWebSocket(rfidCard);
			log.debug("Enrolling fingerprint with ID: {}", fingerprintId);
			return Optional.of(messageResponse);
		} catch (JsonProcessingException e) {
			log.error("Error enrolling fingerprint with ID: {}", fingerprintId, e);
			return Optional.empty();
		}
	}
}

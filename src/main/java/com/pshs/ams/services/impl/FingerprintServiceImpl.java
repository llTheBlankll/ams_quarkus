package com.pshs.ams.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pshs.ams.models.dto.custom.MessageDTO;
import com.pshs.ams.models.dto.custom.RFIDCardDTO;
import com.pshs.ams.models.entities.RfidCredential;
import com.pshs.ams.models.enums.AttendanceMode;
import com.pshs.ams.services.interfaces.AttendanceService;
import com.pshs.ams.services.interfaces.FingerprintService;
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
	public Optional<MessageDTO> enrollFingerprint(Integer fingerprintId) {
		if (fingerprintId <= 0) {
			log.debug("Fingerprint ID is invalid: {}", fingerprintId);
			return Optional.empty();
		}

		Optional<RfidCredential> rfidCredential = RfidCredential.find("fingerprintId", fingerprintId).firstResultOptional();

		RFIDCardDTO rfidCardDTO = new RFIDCardDTO();
		if (rfidCredential.isPresent()) {
			rfidCardDTO.setHashedLrn(rfidCredential.get().getHashedLrn());
			rfidCardDTO.setMode(AttendanceMode.IN);
		}

		try {
			MessageDTO messageDTO = attendanceService.fromWebSocket(rfidCardDTO);
			log.debug("Enrolling fingerprint with ID: {}", fingerprintId);
			return Optional.of(messageDTO);
		} catch (JsonProcessingException e) {
			log.error("Error enrolling fingerprint with ID: {}", fingerprintId, e);
			return Optional.empty();
		}
	}
}

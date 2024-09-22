package com.pshs.ams.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pshs.ams.models.dto.custom.RFIDCardDTO;
import com.pshs.ams.services.RealTimeAttendanceService;
import com.pshs.ams.services.interfaces.AttendanceService;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.jboss.logging.Logger;
import org.modelmapper.ModelMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@ServerEndpoint(value = "/ws/attendances")
public class AttendanceWebSocket {

	private final Map<String, Session> sessions = new ConcurrentHashMap<>();

	@Inject
	AttendanceService attendanceService;

	@Inject
	Logger logger;

	private final ModelMapper mapper = new ModelMapper();

	@OnOpen
	public void onOpen(Session session) {
		logger.debug("Session opened: " + session.getId());
		sessions.put(session.getId(), session);
	}

	@OnClose
	public void onClose(Session session) {
		try {
			sessions.remove(session.getId());
		} catch (Exception e) {
			logger.error("Error while removing session: " + session.getId());
		}
	}

	@OnError
	public void onError(Throwable t) {
		logger.error(t.getMessage());
	}


	@OnMessage
	public void onMessage(String message, Session session) {
		logger.debug("Received message: " + message + " from session: " + session.getId());
		try {
			RFIDCardDTO rfidCard = new ObjectMapper().readValue(message, RFIDCardDTO.class);
			// Validate
			if (rfidCard.getMode() == null) {
				logger.debug("Mode is null");
			} else if (rfidCard.getHashedLrn() == null) {
				logger.debug("Hashed Lrn is null");
			} else {
				// Run from the blocking thread using vertx executeBlocking
				Uni.createFrom().item(Unchecked.supplier(() -> {
						try {
							return this.attendanceService.fromWebSocket(rfidCard);
						} catch (JsonProcessingException e) {
							throw new RuntimeException(e);
						}
					}))
					.emitOn(Infrastructure.getDefaultExecutor())
					.runSubscriptionOn(Infrastructure.getDefaultExecutor())
					.subscribe().with(
						Unchecked.consumer(result -> {
							logger.debug("Scan Result: " + result);
							RemoteEndpoint.Async async = session.getAsyncRemote();
							try {
								async.sendText(
									new ObjectMapper().writeValueAsString(result)
								);
							} catch (JsonProcessingException e) {
								throw new RuntimeException(e);
							}
						}),
						failure -> {
							logger.error(failure.getMessage());
						}
					);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
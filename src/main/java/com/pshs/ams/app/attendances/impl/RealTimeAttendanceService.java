package com.pshs.ams.app.attendances.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class RealTimeAttendanceService {

	@Inject
	Logger logger;

	private final Map<String, Session> sessions = new ConcurrentHashMap<>();

	public void registerSession(Session session) {
		logger.debug("Registering session: " + session.getId());
		sessions.put(session.getId(), session);
	}

	public void unregisterSession(Session session) {
		logger.debug("Unregistering session: " + session.getId());
		if (session.getId() != null) {
			sessions.remove(session.getId());
		}
	}

	public void broadcastMessage(String message) {
		logger.debug("Broadcasting message: " + message);
		sessions.forEach((key, session) -> {
			logger.debug("Sending message: " + message + " to session: " + key);
			session.getAsyncRemote().sendText(message);
		});
	}

}

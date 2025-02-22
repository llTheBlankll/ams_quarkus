package com.pshs.ams.websockets;

import com.pshs.ams.app.attendances.impl.RealTimeAttendanceService;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.jboss.logging.Logger;

@ServerEndpoint(value = "/ws/real-time-attendances")
public class RealTimeAttendanceWebSocket {

	@Inject
	RealTimeAttendanceService realTimeAttendanceService;

	@Inject
	Logger logger;

	@OnOpen
	public void onOpen(Session session) {
		realTimeAttendanceService.registerSession(session);
	}

	@OnClose
	public void onClose(Session session) {
		realTimeAttendanceService.unregisterSession(session);
	}

	@OnError
	public void onError(Throwable t) {
		logger.error(t.getMessage());
	}
}

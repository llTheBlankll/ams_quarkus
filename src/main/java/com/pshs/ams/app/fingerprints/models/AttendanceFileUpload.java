package com.pshs.ams.app.fingerprints.models;

import jakarta.ws.rs.FormParam;

import java.io.InputStream;

public class AttendanceFileUpload {

	@FormParam("file")
	public InputStream file;
}

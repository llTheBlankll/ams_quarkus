package com.pshs.ams.global.models.custom;

import org.jboss.resteasy.reactive.multipart.FileUpload;

import jakarta.ws.rs.FormParam;

public class FileUploadInput {
	@FormParam("file")
	public FileUpload file;
}

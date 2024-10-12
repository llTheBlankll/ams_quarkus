package com.pshs.ams.models.dto.custom;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public class FileUploadInput {
	@RestForm("files")
	public FileUpload file;
}
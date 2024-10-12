package com.pshs.ams.models.dto.custom;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.util.List;

public class FileUploadInput {
	@RestForm("files")
	public List<FileUpload> files;
}
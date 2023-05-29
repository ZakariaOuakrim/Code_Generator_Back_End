package com.stage.code_gen.Services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;

@Service
public class CompressionService {
	public void compressFolder(String folderPath, String zipFilePath) throws IOException {
		File folder = new File(folderPath);
		try (FileOutputStream fos = new FileOutputStream(zipFilePath); ZipOutputStream zos = new ZipOutputStream(fos)) {
			compressFolder(folder, folder.getName(), zos);
		}
	}

	private void compressFolder(File folder, String parentFolderName, ZipOutputStream zos) throws IOException {
		for (File file : folder.listFiles()) {
			if (file.isFile()) {
				ZipEntry zipEntry = new ZipEntry(parentFolderName + "/" + file.getName());
				zos.putNextEntry(zipEntry);

				try (FileInputStream fis = new FileInputStream(file)) {
					byte[] buffer = new byte[1024];
					int length;
					while ((length = fis.read(buffer)) > 0) {
						zos.write(buffer, 0, length);
					}
				}

				zos.closeEntry();
			} else if (file.isDirectory()) {
				String subFolderName = parentFolderName + "/" + file.getName();
				compressFolder(file, subFolderName, zos);
			}
		}
	}
}

package org.nuxeo.google.drive.api;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import org.nuxeo.ecm.core.api.DocumentModel;

import com.google.api.services.drive.model.File;

public interface GoogleDriveService {
	
	/**
	 * Add a file in Google Drive
	 * 
	 * @param fileNode Node of the file to add
	 * @return ID of the added file
	 * @throws Exception
	 */
	public File addFileInGoogleDrive(DocumentModel doc) throws Exception;

	/**
	 * Get document content from Google Drive
	 * 
	 * @param documentId
	 *            Id of the document in Google Drive
	 * @param mimeType
	 *            Mimetype of the document
	 * @return InputStream of the content of the document
	 * @throws IOException
	 */
	public File getFile(String fileId) throws IOException, GeneralSecurityException;

	/**
	 * Get file content from Google Drive
	 * 
	 * @param fileId Id of the file in Google Drive
	 * @param mimeType Mimetype of the file
	 * @return InputStream of the content of the file
	 * @throws IOException
	 */
	public InputStream getFileContent(String fileId, String mimeType) throws IOException, GeneralSecurityException;

	/**
	 * Share a file with an user
	 * @param fileId
	 * @param userEmail
	 * @throws Exception
	 */
	public void shareFileWith(String fileId, String userEmail) throws Exception;

	/**
	 * Share a file with the master Google account
	 * @param fileId
	 * @param userEmail
	 * @throws Exception
	 */
	public void shareFileWithMasterAccount(String fileId) throws Exception;

	/**
	 * Remove a file from Google Drive
	 * 
	 * @param fileId The ID of the file to remove
	 * @throws IOException
	 */
	public void removeFileInGoogleDrive(String fileId) throws IOException, GeneralSecurityException;
}

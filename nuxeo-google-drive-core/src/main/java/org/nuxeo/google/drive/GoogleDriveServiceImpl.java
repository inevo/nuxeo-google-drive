package org.nuxeo.google.drive;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.google.drive.api.GoogleDriveService;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Insert;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;

public class GoogleDriveServiceImpl extends DefaultComponent implements GoogleDriveService {
	private static final Log log = LogFactory.getLog(GoogleDriveServiceImpl.class);
	
    public static final String CONFIGURATION_EP = "configuration";
    
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();

	private String serviceEmail;
	private String accountEmail;
	private String privateKeyFilePath;

	private Drive driveService;

    @Override
    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor)
            throws Exception {
        if (CONFIGURATION_EP.equals(extensionPoint)) {
            ConfigurationDescriptor config = (ConfigurationDescriptor) contribution;
            serviceEmail = config.getServiceEmail();
            accountEmail = config.getAccountEmail();
            privateKeyFilePath = config.getPrivateKey();	
        }
    }

	private Drive getDriveService() throws GeneralSecurityException, IOException {

		if (driveService == null) {
			GoogleCredential credential = new GoogleCredential.Builder()
				.setTransport(HTTP_TRANSPORT) //
				.setJsonFactory(JSON_FACTORY) //
				.setServiceAccountId(this.serviceEmail) //
				.setServiceAccountScopes(DriveScopes.DRIVE) //
				.setServiceAccountPrivateKeyFromP12File(new java.io.File(this.privateKeyFilePath)) //
				.build();

			driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build();
		}

		return driveService;
	}

	public File addFileInGoogleDrive(DocumentModel doc) throws Exception {

		Drive driveService = getDriveService();

		if (!doc.hasSchema("file")) {
			// TODO
			return null;
		}
		
        Blob blob = (Blob) doc.getProperty("file", "content");
        String mimeType = blob.getMimeType();
        
		// Insert a file
		File body = new File();
		body.setTitle(doc.getName());
		body.setMimeType(mimeType);
		
		// TODO files are created on file system, handle it right...
		java.io.File fileContent = new java.io.File(doc.getName());
		blob.transferTo(fileContent);
		
		FileContent mediaContent = new FileContent(mimeType, fileContent);
		
		Insert insert = driveService.files().insert(body, mediaContent);
		insert.getMediaHttpUploader().setDirectUploadEnabled(true);
		File file = insert.setConvert(true).execute();
        
		return file;
	}

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
	public File getFile(String fileId) throws IOException, GeneralSecurityException {
		Drive driveService = getDriveService();

		return driveService.files().get(fileId).execute();
	}

	/**
	 * Get file content from Google Drive
	 * 
	 * @param fileId Id of the file in Google Drive
	 * @param mimeType Mimetype of the file
	 * @return InputStream of the content of the file
	 * @throws IOException
	 */
	public InputStream getFileContent(String fileId, String mimeType) throws IOException, GeneralSecurityException {
		Drive driveService = getDriveService();

		File file = getFile(fileId);

		String url = (String) file.getExportLinks().get(mimeType);
		if (url != null && url.length() > 0) {
			HttpResponse resp = driveService.getRequestFactory().buildGetRequest(new GenericUrl(url)).execute();
			return resp.getContent();
		} else {
			throw new IOException("No export URL available for the file " + fileId + " with the mimetype " + mimeType);
		}
	}

	/**
	 * Share a file with an user
	 * @param fileId
	 * @param userEmail
	 * @throws Exception
	 */
	public void shareFileWith(String fileId, String userEmail) throws Exception {
		Drive driveService = getDriveService();

		Permission newPermission = new Permission();

		newPermission.setValue(userEmail);
	    newPermission.setType("user");
	    newPermission.setRole("writer");

		driveService.permissions().insert(fileId, newPermission).setSendNotificationEmails(false).execute();
	}

	/**
	 * Share a file with the master Google account
	 * @param fileId
	 * @param userEmail
	 * @throws Exception
	 */
	public void shareFileWithMasterAccount(String fileId) throws Exception {
		shareFileWith(fileId, this.accountEmail);
	}

	/**
	 * Remove a file from Google Drive
	 * 
	 * @param fileId The ID of the file to remove
	 * @throws IOException
	 */
	public void removeFileInGoogleDrive(String fileId) throws IOException, GeneralSecurityException {
		Drive driveService = getDriveService();

		driveService.files().delete(fileId).execute();
	}
}

package org.nuxeo.google.drive;

import static org.jboss.seam.ScopeType.EVENT;

import java.io.Serializable;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.google.drive.api.GoogleDriveService;

import com.google.api.services.drive.model.File;

@Name("googleDriveActions")
@Scope(EVENT)
public class GoogleDriveActions implements Serializable {

    @In(create = true, required = true)
    protected transient CoreSession documentManager;
    
    @In(create = true)
    protected NavigationContext navigationContext;
    
    @In(create = true, required = true)
    protected NuxeoPrincipal currentNuxeoPrincipal;
    
    @In(create = true, required = true)
    protected GoogleDriveService googleDriveService;
    
    private static final long serialVersionUID = 1L;

    public void editFile(DocumentModel doc) throws Exception {
        
        File googleDriveFile = null;
        String googleDriveFileID = null;
        
        if (doc.hasSchema("googledrive")) {
        	googleDriveFileID =  (String) doc.getProperty("googledrive", "googledriveid");
        }
        
        if(googleDriveFileID == null) {
			// insert doc in google drive
			googleDriveFile = googleDriveService.addFileInGoogleDrive(doc);
			googleDriveFileID = googleDriveFile.getId();

			// share the document with the main Google account
			googleDriveService.shareFileWithMasterAccount(googleDriveFileID);

			// add google drive file id
			doc.setProperty("googledrive", "googledriveid", googleDriveFileID);
	        documentManager.saveDocument(doc);
	        documentManager.save();
	        
		} else {
			// get the Google Drive file id
			googleDriveFileID = (String) doc.getProperty("googledrive", "googledriveid");

			// get the Google Drive file
			googleDriveFile = googleDriveService.getFile(googleDriveFileID);					
		}
        
        String userEmail = currentNuxeoPrincipal.getEmail();

        if(userEmail == null || userEmail.isEmpty()) {
			throw new Exception("Email of user " + currentNuxeoPrincipal.getName() + " is empty");
		}
        
        googleDriveService.shareFileWith(googleDriveFileID, userEmail);

        navigationContext.navigateToURL(googleDriveFile.getAlternateLink());
    }


}

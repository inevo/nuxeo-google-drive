/**
 * 
 */

package org.nuxeo.google.drive;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.nuxeo.google.drive.api.GoogleDriveService;
import org.nuxeo.runtime.api.Framework;


@Name("googleDriveService")
@Scope(ScopeType.EVENT)
public class GoogleDriveServiceBean implements Serializable {

    private static final long serialVersionUID = 1L;

    protected GoogleDriveService googleDriveService;

    @Unwrap
    public GoogleDriveService getService() throws Exception {
        if (googleDriveService == null) {
        	googleDriveService = Framework.getService(GoogleDriveService.class);
        }
        return googleDriveService;
    }

}

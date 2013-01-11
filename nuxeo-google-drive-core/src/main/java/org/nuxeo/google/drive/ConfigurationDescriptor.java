package org.nuxeo.google.drive;

import java.io.Serializable;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

@XObject("configuration")
public class ConfigurationDescriptor implements Serializable {
    private static final long serialVersionUID = 1L;

    @XNode("serviceEmail")
    private String serviceEmail;

    @XNode("accountEmail")
    private String accountEmail;

    @XNode("privateKey")
    private String privateKey;

	public String getServiceEmail() {
		return serviceEmail;
	}

	public String getAccountEmail() {
		return accountEmail;
	}

	public String getPrivateKey() {
		return privateKey;
	}
    
    
}

package org.jgn.api.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConfigPropertiesUtil {

	private final String cordaNodeNames = "cordaNodeNames";
	private final String RPCFlowsList = "RPCFlowsList";

	private ConfigProperties config;

	public ConfigPropertiesUtil() {
		config = new ConfigProperties();

	}

	private List<String> getPropertieAsList(String propertie) {
		return ((propertie != null) || (propertie.length() > 0)) ? Arrays.asList(propertie.split("\\s*,\\s*"))
				: Collections.emptyList();
	}

	public List<String> getNodesList() throws IOException {
		return getPropertieAsList(getPropValue(cordaNodeNames));
	}

	public List<String> getNodeRPCDetails(String nodeName) throws IOException {
		return getPropertieAsList(getPropValue(nodeName));
	}

	public List<String> getFlowsList() throws IOException {
		return getPropertieAsList(getPropValue(RPCFlowsList));
	}
	
	public String getPropValue(String prop) throws IOException
	{
		 return config.getPropertyValue(prop);
	}

}


//https://stackoverflow.com/questions/9737812/properties-file-with-a-list-as-the-value-for-an-individual-key/9738132
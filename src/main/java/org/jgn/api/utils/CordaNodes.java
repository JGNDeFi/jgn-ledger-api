package org.jgn.api.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import net.corda.client.rpc.CordaRPCClient;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.utilities.NetworkHostAndPort;

public class CordaNodes {

	HashMap<Integer, CordaRPCOps> nodeMap = new HashMap<Integer, CordaRPCOps>();
	HashMap<String, Integer> nodex500NametoID = new HashMap<String, Integer>();
	
	HashMap<String, String> flowNodeMap = new HashMap<String, String>();

	public CordaNodes() throws IOException {

		ConfigPropertiesUtil configUtil = new ConfigPropertiesUtil();

		for (String cordaNode : configUtil.getNodesList()) {

			List<String> connProperties = configUtil.getNodeRPCDetails(cordaNode);

			String[] connPropertiesArr = connProperties.toArray(new String[connProperties.size()]);

			int nodeId = Integer.parseInt(connPropertiesArr[0]);
			String IPAddress = connPropertiesArr[1];
			String port = connPropertiesArr[2];
			String uname = connPropertiesArr[3];
			String pwd = connPropertiesArr[4];

			nodeMap.put(nodeId, createNewRPCNode(IPAddress, port, uname, pwd));
			CordaRPCOps node = nodeMap.get(nodeId);
			String x500Name = node.nodeInfo().getLegalIdentitiesAndCerts().get(0).getName().toString();
			nodex500NametoID.put(x500Name,nodeId);
		}

		/*for (String flowMap : configUtil.getFlowsList()) {

			flowNodeMap.put(flowMap, configUtil.getPropValue(flowMap));
		}/*/

	}
	
	public String getX500NameofNode(int nodeId)
	{
		return nodeMap.get(nodeId).nodeInfo().getLegalIdentitiesAndCerts().get(0).getName().toString();
	}
	
	public String getX500NameofNode(CordaRPCOps node)
	{
		return node.nodeInfo().getLegalIdentitiesAndCerts().get(0).getName().toString();
	}
	
	public Party getNodeParty(int nodeId) {
		return nodeMap.get(nodeId).nodeInfo().getLegalIdentities().get(0);
	}
	public Party getNodeParty(CordaRPCOps node) {
		
		return node.nodeInfo().getLegalIdentities().get(0);
	}

	private CordaRPCOps createNewRPCNode(String IPAddress, String Port, String UserName, String Pasword) {
		return new CordaRPCClient(new NetworkHostAndPort(IPAddress, Integer.parseInt(Port))).start(UserName, Pasword)
				.getProxy();
	}

	
	public CordaRPCOps getRPCNodebyX500Name(String name)
	{
		return nodeMap.get(nodex500NametoID.get(name));
	}
	public CordaRPCOps getRPCNode(int nodeId, String flowName) {
		if(nodeId >=0)
			return nodeMap.get(nodeId);
		
		else return nodeMap.get(2);//Always return JGN
			
		//return nodeMap.get(flowNodeMap.get(flowName));
	}

}

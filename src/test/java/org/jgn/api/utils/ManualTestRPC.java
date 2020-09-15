package org.jgn.api.utils;

import java.util.Date;
import java.util.List;

public class ManualTestRPC {
	public static void main(String[] args) throws Exception {
		NodeRPCConnection rpc;
		rpc = new NodeRPCConnection();

		//rpc.sendGetAccountsFlow(-1, false);

		// System.out.println(rpc.sendCreateAccount("a002"));
//		String epochdate = Long.toString(new Date().getTime());
//		
//		String uuid = rpc.sendCreateAccount(epochdate,-1);
//		System.out.println(uuid);
//		rpc.sendAccountQueryByIdFlow(uuid);
		
		
		String UUID = rpc.sendCreateAccountAndShare("TESTBYNAME",3); 
				
		String returnUUID = rpc.sendGetAccountByName(3, false, "TESTBYNAME");
		
		
		if(UUID.equals(returnUUID))
		{
			System.out.println("Working");
		}
		

		List<String> stoList = rpc.sendGetSTOs();
		System.out.println("Total STOs: " + stoList.size());

		for (int i = 0; i < 250; i++) {
			String symbol = Long.toString(new Date().getTime()) + i;
			rpc.sendIssueAssetFlow(1000, symbol);
		}

		stoList = rpc.sendGetSTOs();
		System.out.println("Total STOs: " + stoList.size());
		
		for (int i = 0; i < 250; i++) {
			String symbol = Long.toString(new Date().getTime()) + i;
			rpc.sendCreateAccountAndShare(symbol,3);
		}

	}

}

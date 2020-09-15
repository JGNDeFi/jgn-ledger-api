package org.jgn.api.utils;

import java.util.ArrayList;

public class ManulDeploySetup {

	static String uniqueString = "TEST_002_";
	static int maxAmount = Integer.MAX_VALUE;

	public static void main(String[] args) throws Exception {
		NodeRPCConnection rpc;
		rpc = new NodeRPCConnection();

		// Create JGN Account
		String cordaUUIDJGN = rpc.sendGetAccountByName(2, true, "JGN-G");
		if (cordaUUIDJGN == null) {
			cordaUUIDJGN = rpc.sendCreateAccount("JGN-G", 2, false);
			rpc.doSendShareAccountFlow(cordaUUIDJGN, 2, 3);
		}

		// Create TRUST Account
		String cordaUUIDTrust = rpc.sendGetAccountByName(3, true, "TRUST-G");
		if (cordaUUIDTrust == null) {
			cordaUUIDTrust = rpc.sendCreateAccount("TRUST-G", 3, false);
			rpc.doSendShareAccountFlow(cordaUUIDTrust, 3, 2);
		}

		System.out.println("JGN UUID: " + cordaUUIDJGN);
		System.out.println("Trust UUID: " + cordaUUIDTrust);

		// Issue assets
		rpc.sendIssueAssetFlow(maxAmount, uniqueString);

		ArrayList<String> stoList = rpc.sendGetSTOs();
		if (stoList.size() > 0) {

			System.out.println("Available STO's:  ");
			stoList.forEach((stoSymbol) -> System.out.println(stoSymbol));
		}
		
		System.out.println("TRUST: Symbol: " + uniqueString + " Amount: " + rpc.sendGetBalance(cordaUUIDTrust, uniqueString));

		
		String inv1name = uniqueString + Long.toString(1);
		String inv1 = rpc.sendGetAccountByName(-1, false, inv1name); 
		if (inv1 == null) {
			inv1 = rpc.sendCreateAccountAndShare(inv1name, 1);
		}
		
		String inv2name = uniqueString + Long.toString(2);
		String inv2 = rpc.sendGetAccountByName(-1, false, inv2name); 
		if (inv2 == null) {
			inv2 = rpc.sendCreateAccountAndShare(inv2name, 1);
		}
		

		// Transfer from Trust to Account 1
		String stoTxId = rpc.sendTransfer(cordaUUIDTrust, cordaUUIDJGN, maxAmount-10, uniqueString);
		rpc.sendTransfer(cordaUUIDJGN, inv1,  maxAmount-20, uniqueString);
		 
		rpc.sendTransfer(inv1, inv2,  (maxAmount-40)/2, uniqueString);

	}

}

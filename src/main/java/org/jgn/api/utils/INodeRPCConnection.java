package org.jgn.api.utils;

import java.util.ArrayList;

import net.corda.accounts.states.AccountInfo;
import net.corda.core.concurrent.CordaFuture;

public interface INodeRPCConnection {

	public boolean sendWithdraw(Integer amount, String userId, String STOName);

	public String sendCreateAccountAndShare(String someId, int nodeId);

	public int sendGetBalance(String accountUUID, String Symbol);

	public String sendTransfer(String fromAccount, String toAccount, Integer amount, String symbol);

	public ArrayList<String> sendGetSTOs();

	public boolean sendGetSTOInfo(String sto);

	public CordaFuture<AccountInfo> sendAccountQueryByIdFlow(String accountUUID);
	
	public String sendGetAccountsFlow(int NodeId, boolean onlyLocal);
	
	public void sendShareAccountFlow(String UUID, int sourceNodeId);
	
	public void sendShareAccountInfoWithNodes();
	
	public String sendIssueAssetFlow(int amount, String symbol);
	
	public String sendGetAccountByName(int NodeId, boolean onlyLocal, String accountName);

}


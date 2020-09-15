package org.jgn.api.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.jgn.flows.AccountQueryByIdFlow;
import org.jgn.flows.IssueAssetFlow;
import org.jgn.flows.QueryAssetFlow;
import org.jgn.flows.ShareAccountFlow;
import org.jgn.flows.TransferAssetFlow;
import org.jgn.contracts.AssetState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.messaging.FlowHandle;
import net.corda.core.transactions.SignedTransaction;
import net.corda.accounts.flows.GetAccountsFlow;
import net.corda.accounts.flows.OpenNewAccountFlow;
import net.corda.core.contracts.StateAndRef;
import net.corda.accounts.states.AccountInfo;

public class NodeRPCConnection implements INodeRPCConnection {
	private static final Logger logger = LoggerFactory.getLogger(NodeRPCConnection.class);

	private CordaNodes cordaNodes;

	public NodeRPCConnection() throws Exception {

		cordaNodes = new CordaNodes();
	}

	@Deprecated
	public boolean sendDeposit(Integer amount, String userId, String STOName) {
		logger.info("Forwarding Deposit message to LedgerService: { amount = " + amount + ", userId = " + userId
				+ ", STOName = " + STOName + " }");

		// TODO: Replace with userId
		CordaX500Name cordaX500Name = new CordaX500Name(null, "pine", "Trust of WagyuBeef", "hokkaido", null, "GB");
		// Party otherParty =
		// cordaRPCOperations.wellKnownPartyFromX500Name(cordaX500Name);

		try {
			// TODO: Replace JGNFlow to STODepositFlow
			// cordaRPCOperations.startFlowDynamic(JGNFlow.class, amount, otherParty);
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}
	}

	@Override
	public String sendCreateAccountAndShare(String jgnId, int nodeId) {
		logger.info("Forwarding Transfer message to LedgerService: { someId = " + jgnId + " }");
		return doSendCreateAccount(jgnId, nodeId, true);

	}

	public String sendCreateAccount(String jgnId, int nodeId, boolean shareAccount) {
		logger.info("Forwarding Transfer message to LedgerService: { someId = " + jgnId + " }");
		return doSendCreateAccount(jgnId, nodeId, shareAccount);
	}

	public String doSendCreateAccount(String jgnId, int nodeId, boolean shareAccount) {
		try {
			CordaFuture<StateAndRef<? extends AccountInfo>> data = cordaNodes.getRPCNode(nodeId, "OpenNewAccountFlow")
					.startFlowDynamic(OpenNewAccountFlow.class, jgnId).getReturnValue();
			AccountInfo accountInfo = data.get().getState().getData();

			if (shareAccount)
				sendShareAccountFlow(accountInfo.getAccountId().toString(), nodeId);

			return accountInfo.getAccountId().toString();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	@Override
	public int sendGetBalance(String accountUUID, String Symbol) {
		logger.info("Forwarding GetBalance message to LedgerService: { account = " + accountUUID + " }");

		try {
			List<? extends StateAndRef<? extends AssetState>> data = getNodeofUUID(accountUUID)
					.startFlowDynamic(QueryAssetFlow.class, accountUUID, Symbol).getReturnValue().get();

			AtomicInteger balanceSum = new AtomicInteger(0);
			data.forEach((stateRef) -> balanceSum.getAndAdd(stateRef.getState().getData().getAmount()));

			return balanceSum.get();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return -1;
		}
	}

	@Override
	public String sendTransfer(String fromAccount, String toAccount, Integer amount, String symbol) {
		logger.info("Forwarding Transfer message to LedgerService: { fromAccount = " + fromAccount + ", toAccount = "
				+ toAccount + ", amount = " + amount + " }");

		try {
			CordaFuture<SignedTransaction> signedTx = getNodeofUUID(fromAccount)
					.startFlowDynamic(TransferAssetFlow.Initiator.class, amount, symbol, fromAccount, toAccount)
					.getReturnValue();

			return signedTx.get().getTx().getId().toString();

		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	CordaRPCOps getNodeofUUID(String UUID) {
		try {
			CordaFuture<AccountInfo> accountInfo = sendAccountQueryByIdFlow(UUID);
			String accountX500Name = accountInfo.get().getAccountHost().getName().toString();
			logger.info("Using Node : { " + accountX500Name + " }");
			return cordaNodes.getRPCNodebyX500Name(accountX500Name);
		} catch (Exception e) {
			return null;

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgn.api.utils.INodeRPCConnection#sendAccountQueryByIdFlow(java.lang.
	 * String) networkMapSnapshot https://docs.corda.net/api/javadoc/index.html
	 * https://docs.corda.net/api-rpc.html List<NodeInfo>nodes
	 * =cordaNodes.getRPCNode(-1,"").networkMapSnapshot(); NodeInfo node1
	 * =nodes.get(0); CordaX500Name tocompare =
	 * node1.getLegalIdentitiesAndCerts().get(0).getName(); AccountInfo accountInfo
	 * = data.get(); String name =
	 * accountInfo.getAccountHost().getName().getOrganisation();
	 */
	@Override
	public CordaFuture<AccountInfo> sendAccountQueryByIdFlow(String accountUUID)// Calls trustee by default
	{
		try {

			CordaFuture<AccountInfo> accountInfo = cordaNodes.getRPCNode(-1, "")
					.startFlowDynamic(AccountQueryByIdFlow.class, accountUUID).getReturnValue();

			return accountInfo;

		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}

	}

	@Override
	public ArrayList<String> sendGetSTOs() {
		logger.info("Forwarding sendGetSTOs message to LedgerService: {  }");

		ArrayList<String> stoList = new ArrayList<String>();
		try {

			List<? extends StateAndRef<? extends AssetState>> data = cordaNodes.getRPCNode(3, "QueryAssetFlow")
					.startFlowDynamic(QueryAssetFlow.class, null, null).getReturnValue().get();

			data.forEach((assetSTO) -> stoList.add(assetSTO.getState().getData().getSymbol()));
			return stoList;
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	public void close() {
		// connection.notifyServerAndClose();
	}

	@Override
	public String sendGetAccountsFlow(int nodeId, boolean onlyLocal) {

		try {
			List<? extends StateAndRef<? extends AccountInfo>> data = cordaNodes.getRPCNode(nodeId, "")
					.startFlowDynamic(GetAccountsFlow.class, onlyLocal).getReturnValue().get();

			StateAndRef<? extends AccountInfo> acc = data.get(0);
			AccountInfo accountInfo = acc.getState().getData();
			return accountInfo.getAccountId().toString();

		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public String sendGetAccountByName(int nodeId, boolean onlyLocal, String accountName) {

		try {
			List<? extends StateAndRef<? extends AccountInfo>> data = cordaNodes.getRPCNode(nodeId, "")
					.startFlowDynamic(GetAccountsFlow.class, onlyLocal).getReturnValue().get();

			boolean isPresent = data.parallelStream()
					.anyMatch(account -> accountName.equals(account.getState().getData().getAccountName().toString()));

			if (isPresent) {
				Optional<StateAndRef<? extends AccountInfo>> result = (Optional<StateAndRef<? extends AccountInfo>>) data
						.parallelStream()
						.filter(account -> accountName.equals(account.getState().getData().getAccountName().toString()))
						.findFirst();

				return result.get().getState().getData().getAccountId().toString();
			} else
				throw new Exception("Account " + accountName + " Not Found");

		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	@Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jgn.api.utils.INodeRPCConnection#sendShareAccountFlow(java.lang.String,
	 * int)
	 * 
	 * To share Account A1 in Node1(Source), to Node2(Destination) RPC call on
	 * SourceNode with parameters (A1, Node2)
	 */
	public void sendShareAccountFlow(String UUID, int sourceNodeId) {

		// Get JGN Party
		doSendShareAccountFlow(UUID, sourceNodeId, -1);

	}

	public void sendShareAccountFlow(String UUID, int sourceNodeId, int nodeId) {
		doSendShareAccountFlow(UUID, sourceNodeId, nodeId);

	}

	public void doSendShareAccountFlow(String UUID, int sourceNodeId, int nodeId) {
		try {

			Party destNode = cordaNodes.getRPCNode(nodeId, "").nodeInfo().getLegalIdentities().get(0);
			logger.info("Sahre account: {UUID: " + UUID + " , x500Name: " + destNode.toString() + " }");
			cordaNodes.getRPCNode(sourceNodeId, "").startFlowDynamic(ShareAccountFlow.class, UUID, destNode);
		} catch (Exception e) {
			logger.error(e.getMessage());
			// return null;
		}

	}

	@Override
	public void sendShareAccountInfoWithNodes() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean sendWithdraw(Integer amount, String userId, String STOName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean sendGetSTOInfo(String sto) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String sendIssueAssetFlow(int amount, String symbol) {

		int nodeToUse = 3;// use the default trust
		return doIssueAssetFlow(nodeToUse, amount, symbol);
	}

	public String doIssueAssetFlow(int nodeId, int amount, String symbol) {

		try {
			FlowHandle<StateAndRef<? extends AssetState>> result = cordaNodes.getRPCNode(nodeId, "")
					.startFlowDynamic(IssueAssetFlow.Initiator.class, amount, symbol);
			return result.getReturnValue().get().getRef().getTxhash().toString();

		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}

	}

	public void runInitialSetup() {

		String JGN_ACCOUNT_NAME = "JGN0";
		String TRUST_ACCOUNT_NAME = "TRUST0";
		int maxAmount = Integer.MAX_VALUE;
		//For new STO, change the symbol
		String uniqueString = "STO2";

		// Create JGN Account
		String cordaUUIDJGN = sendGetAccountByName(2, true, JGN_ACCOUNT_NAME);

		logger.error("Retrieved JGNUUID: "+cordaUUIDJGN);
		if (cordaUUIDJGN == null) {
			logger.error("Creating JGN account in JGN");
			cordaUUIDJGN = sendCreateAccount(JGN_ACCOUNT_NAME, 2, false);
			logger.error("JGN account create: "+cordaUUIDJGN);
			doSendShareAccountFlow(cordaUUIDJGN, 2, 3);
		}

		// Create TRUST Account
		String cordaUUIDTrust = sendGetAccountByName(3, true, TRUST_ACCOUNT_NAME);
		logger.error("Retrieved Trust UUID: "+cordaUUIDTrust);
		if (cordaUUIDTrust == null) {
			logger.error("Creating trust account in Trust");
			cordaUUIDTrust = sendCreateAccount(TRUST_ACCOUNT_NAME, 3, false);
			logger.error("Trust account create: "+cordaUUIDTrust);
			doSendShareAccountFlow(cordaUUIDTrust, 3, 2);
		}

		boolean isSTOIssued = false;
		ArrayList<String> stoList = sendGetSTOs();
		for (String stoSymbol : stoList) {
			if (stoSymbol.equals(uniqueString))
				isSTOIssued = true;
		}

		// Issue assets
		if (!isSTOIssued) {
			sendIssueAssetFlow(maxAmount, uniqueString);
			String stoTxId = sendTransfer(cordaUUIDTrust, cordaUUIDJGN, maxAmount - 100, uniqueString);
		}
		
		logger.error("JGN account '"+uniqueString+"' balance: "+sendGetBalance(cordaUUIDJGN, uniqueString));
	}

}

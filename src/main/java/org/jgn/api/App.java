package org.jgn.api;

import java.util.ArrayList;
import org.jgn.api.proto.ApiProto.*;
import org.jgn.api.utils.ConfigProperties;
import org.jgn.api.utils.NodeRPCConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

public class App {
	static NodeRPCConnection rpc;
	static Logger logger;

	private static class Worker extends Thread {
		private ZContext context;

		private Worker(ZContext context) {
			this.context = context;
		}

		@Override
		public void run() {
			ZMQ.Socket socket = context.createSocket(SocketType.REP);
			socket.connect("inproc://workers");

			while (true) {
				byte[] reply = socket.recv(0);
				try {
					boolean status = false;

					// Parse request
					Request request = Request.parseFrom(reply);
					Response.Builder response = Response.newBuilder();
					ResponseHeader.Builder resHeader = ResponseHeader.newBuilder();
					ResponseBody.Builder resBody = ResponseBody.newBuilder();

					switch (request.getHeader().getType()) {
					case DEPOSIT:
						// TODO: Deposit workflow
						/*
						 * Deposit deposit = request.getBody().getDeposit(); status =
						 * rpc.sendDeposit(deposit.getAmount(), deposit.getUserId(),
						 * deposit.getSTOName()); break;
						 */
						throw new Exception("Flow not implemented");

					case WITHDRAW:
						/*
						 * Withdraw withdraw = request.getBody().getWithdraw(); status =
						 * rpc.sendWithdraw(withdraw.getAmount(), withdraw.getUserId(),
						 * withdraw.getSTOName());
						 * resHeader.setType(ResponseHeader.ResponseType.WITHDRAW);
						 * 
						 * RespWithdraw.Builder resWithdraw = RespWithdraw.newBuilder();
						 * resWithdraw.setMessage("Test: Message from Coarda");
						 * resBody.setWithdraw(resWithdraw); break;
						 */
						throw new Exception("Flow not implemented");

					case CREATE_ACCOUNT:
						resHeader.setType(ResponseHeader.ResponseType.CREATE_ACCOUNT);
						CreateAccount createAccount = request.getBody().getCreateAccount();

						int nodeId = request.getBody().getNodeInfo().getNodeId().getNumber();
						logger.info("Create Account Name from Exchange " + createAccount.getName());
						logger.info("Node " + nodeId);

						String accnttId = rpc.sendCreateAccountAndShare(createAccount.getName(), nodeId);
						RespCreateAccount.Builder resCreateAcc = RespCreateAccount.newBuilder();

						if (accnttId != null && !accnttId.isEmpty()) {
							status = true;
							resCreateAcc.setUuId(accnttId);
							resBody.setCreateAccount(resCreateAcc);
						}
						break;

					case GET_BALANCE:
						resHeader.setType(ResponseHeader.ResponseType.GET_BALANCE);
						GetBalance getBalance = request.getBody().getGetBalance();
						String accountUUID = getBalance.getAccount();
						String stoName = getBalance.getSymbol();

						int balance = rpc.sendGetBalance(accountUUID, stoName);
						RespGetBalance.Builder resBalance = RespGetBalance.newBuilder();

						if (balance >= 0) {
							status = true;
							resBalance.setAmount(balance);
							resBody.setGetBalance(resBalance);
						}
						break;

					case TRANSFER:
						resHeader.setType(ResponseHeader.ResponseType.TRANSFER);
						Transfer transfer = request.getBody().getTransfer();

						String txID = rpc.sendTransfer(transfer.getFromAccount(), transfer.getToAccount(),
								transfer.getAmount(), transfer.getSymbol());
						logger.info("Tx ID: " + txID);
						RespTransfer.Builder respTranser = RespTransfer.newBuilder();
						if (txID != null && !txID.isEmpty()) {
							status = true;
							respTranser.setTxId(txID);
							resBody.setTransfer(respTranser);
						}
						break;

					case GET_STOS:
						resHeader.setType(ResponseHeader.ResponseType.GET_STOS);
						RespGetSTOs.Builder resGetSTOs = RespGetSTOs.newBuilder();

						ArrayList<String> stoList = rpc.sendGetSTOs();

						if (stoList != null && stoList.size() > 0) {
							status = true;
							stoList.forEach((sto) -> resGetSTOs.addSto(sto));
							resBody.setStos(resGetSTOs);
						}

						break;

					case GET_STO_INFO:
						throw new Exception("Flow not implemented");
						/*
						 * resHeader.setType(ResponseHeader.ResponseType.GET_STO_INFO); STOInfo stoInfo
						 * = request.getBody().getStoInfo(); // status =
						 * rpc.sendTransfer(transfer.getFromAccount(), // transfer.getToAccount());
						 * break;
						 */
					case GET_ONE_ACCOUNT_IN_NODE:
						resHeader.setType(ResponseHeader.ResponseType.GET_ONE_ACCOUNT_IN_NODE);
						nodeId = request.getBody().getNodeInfo().getNodeId().getNumber();

						accnttId = rpc.sendGetAccountsFlow(nodeId, true);
						RespGetOneAccountInNode.Builder respGetAccount = RespGetOneAccountInNode.newBuilder();

						if (accnttId != null && !accnttId.isEmpty()) {
							status = true;
							respGetAccount.setUuId(accnttId);
							resBody.setAccUUID(respGetAccount);
						}
						break;
					case GET_UUID_BY_NAME:
						resHeader.setType(ResponseHeader.ResponseType.GET_UUID_BY_NAME);
						GetAccountUUIDByName getUUID = request.getBody().getGetUUIDbyName();

						nodeId = request.getBody().getNodeInfo().getNodeId().getNumber();
						logger.info("Get UUID of Account " + getUUID.getName());
						logger.info("Node " + nodeId);

						String accUUID = rpc.sendGetAccountByName(nodeId, false, getUUID.getName());
						RespGetAccountUUIDByName.Builder resgetUUIDbyName = RespGetAccountUUIDByName.newBuilder();

						if (accUUID != null && !accUUID.isEmpty()) {
							status = true;
							resgetUUIDbyName.setUuid(accUUID);
							resBody.setGetUUIDbyName(resgetUUIDbyName);
						}
						break;

					default:
						// Method is not defined
						throw new Exception("Invalid method");
					}

					// If everything went well, set response status to OK

					resHeader.setVersion("1.1");
					resHeader
							.setStatus(status ? ResponseHeader.ResponseStatus.OK : ResponseHeader.ResponseStatus.ERROR);

					response.setHeader(resHeader);
					response.setBody(resBody);

					socket.send(response.build().toByteArray(), 0);
				} catch (Exception e) {
					// If there was a problem, set response status to ERROR and send error message
					Response.Builder response = Response.newBuilder();
					ResponseHeader.Builder resHeader = ResponseHeader.newBuilder();

					resHeader.setVersion("1.1");
					resHeader.setStatus(ResponseHeader.ResponseStatus.ERROR);
					resHeader.setMessage(e.getMessage());
					response.setHeader(resHeader);
					socket.send(response.build().toByteArray(), 0);
				}

			}
		}
	}

	public static void main(String[] args) throws Exception {
		logger = LoggerFactory.getLogger(App.class);
		ConfigProperties config = new ConfigProperties();

		final String protocol = config.getPropertyValue("zeroMQProtocol");
		final String host = config.getPropertyValue("zeroMQHost");
		final String port = config.getPropertyValue("zeroMQPort");
		final String addr = protocol + "://" + host + ":" + port;

		rpc = new NodeRPCConnection();
		logger.info(
				"Multithreader server: " + Runtime.getRuntime().availableProcessors() + " threads. Thread per core");
		logger.info("Listening on " + addr);
		System.out.println("Started");
		
		rpc.runInitialSetup();

		try (ZContext context = new ZContext()) {
			Socket clients = context.createSocket(SocketType.ROUTER);
			clients.bind(addr);

			Socket workers = context.createSocket(SocketType.DEALER);
			workers.bind("inproc://workers");

			// Creates Multithreaded Server
			for (int thread_nbr = 0; thread_nbr < Runtime.getRuntime().availableProcessors(); thread_nbr++) {
				Thread worker = new Worker(context);
				worker.start();
			}

			// Connect work threads to client threads via a queue
			ZMQ.proxy(clients, workers, null);
		}
	}

}

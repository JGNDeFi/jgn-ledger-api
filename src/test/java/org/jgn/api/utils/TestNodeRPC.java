package org.jgn.api.utils;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TestNodeRPC {

	NodeRPCConnection rpc;
	String prefix = "TEST_";

	@Before
	public void setUp() throws Exception {
		rpc = new NodeRPCConnection();
	}

	@Test
	@Ignore
	// Create account in JGN
	public void testSendCreateAccountInJGN() {
		String symbol = prefix + Long.toString(new Date().getTime());
		String uuid = rpc.sendCreateAccount(symbol, 2, false);
		assertTrue(uuid != null && !uuid.isEmpty());
	}

	@Test
	@Ignore
	// Create account in TRUST
	public void testSendCreateAccountInTrust() {
		String symbol = prefix + Long.toString(new Date().getTime());
		String uuid = rpc.sendCreateAccount(symbol, 3, false);
		assertTrue(uuid != null && !uuid.isEmpty());
	}

	@Test
	@Ignore
	// Issue STO in TRUST
	public void testSendIssueAssetFlow() {
		String symbol = Long.toString(new Date().getTime());
		String txId = rpc.sendIssueAssetFlow(1000, symbol);
		assertTrue(txId != null && !txId.isEmpty());
	}

	@Test
	@Ignore
	// Create account in Trustee and share it with JGN
	public void testSendCreateAccount() {
		String symbol = prefix + Long.toString(new Date().getTime());
		String uuid = rpc.sendCreateAccountAndShare(symbol, 1);
		assertTrue(uuid != null && !uuid.isEmpty());
	}

	@Test
	@Ignore
	public void testGetAccountByName() {
		String UUID = rpc.sendCreateAccountAndShare("TEST_GetAccountByName", 3);

		String returnUUID = rpc.sendGetAccountByName(3, false, "TEST_GetAccountByName");

		assertTrue(UUID.equals(returnUUID));
	}

}

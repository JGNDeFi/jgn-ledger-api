package org.jgn.api.utils;

import java.io.Serializable;

import net.corda.accounts.states.AccountInfo;

public class CordaReturnViews {

}

class AccountInfoView {
	String accountName;
	String accountHost;
	String accountId;

	public AccountInfoView(String accountName, String accountHost, String accountId) {
		this.accountName = accountName;
		this.accountHost = accountHost;
		this.accountId = accountId;

	}
	
	public AccountInfoView(AccountInfo acc)	{
		this.accountName = acc.getAccountName();
		this.accountHost = acc.getAccountHost().toString();
		this.accountId = acc.getAccountId().toString();
	}
	
	@Override
	public String toString(){
	return "accountName: " + accountName + 
			"; accountHost: " + accountHost + 
			"; accountId: " + accountId ;
	}
	
	public String accountId() {
	      return accountId;
	    }
}
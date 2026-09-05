package com.damalert.ddas.monitoring.application;

public interface BrokerCredentialProvisioner {
	String provision(String deviceId, String deviceKey);
	void rotate(String brokerReference, String deviceId, String deviceKey);
	void disable(String brokerReference);
	void enable(String brokerReference, String deviceId);
	void revoke(String brokerReference);
}

package com.damalert.ddas.monitoring.application;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Stable application-side broker reference. Dev 4's Mosquitto deployment adapter can replace this bean
 * without exposing device keys to domain code or audit logs.
 */
@Component
@Profile("!standalone")
public class DatabaseBackedBrokerCredentialProvisioner implements BrokerCredentialProvisioner {
	@Override
	public String provision(String deviceId, String deviceKey) { return "mosquitto:" + deviceId; }
	@Override
	public void rotate(String brokerReference, String deviceId, String deviceKey) { }
	@Override
	public void disable(String brokerReference) { }
	@Override
	public void enable(String brokerReference, String deviceId) { }
	@Override
	public void revoke(String brokerReference) { }
}

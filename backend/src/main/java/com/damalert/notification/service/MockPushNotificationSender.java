package com.damalert.notification.service;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class MockPushNotificationSender implements PushNotificationSender {

	@Override
	public boolean sendPush(String pushToken, String payload) {
		return ThreadLocalRandom.current().nextBoolean();
	}
}
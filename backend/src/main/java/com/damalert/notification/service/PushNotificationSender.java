package com.damalert.notification.service;

public interface PushNotificationSender {

	boolean sendPush(String pushToken, String payload);
}
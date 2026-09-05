package com.damalert.notification.entity;

public enum OutboxStatus {
	PENDING,
	PROCESSING,
	SENT,
	FAILED,
	DEAD
}
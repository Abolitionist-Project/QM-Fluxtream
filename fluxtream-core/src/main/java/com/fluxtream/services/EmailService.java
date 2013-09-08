package com.fluxtream.services;

public interface EmailService {

	void sendEmail(String from, String to, String subject, String message, String... cc);
}

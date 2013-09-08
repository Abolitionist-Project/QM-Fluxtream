package com.fluxtream.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.fluxtream.services.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

	@Autowired
	JavaMailSender mailSender;

	@Override
	public void sendEmail(String from, String to, String subject, String text, String... cc) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(from);
		message.setTo(to);
		message.setCc(cc);
		message.setSubject(subject);
		message.setText(text);
		mailSender.send(message);
	}

}

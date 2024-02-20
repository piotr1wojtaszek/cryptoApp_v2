package com.piwo.cryptoApp.service;

import jakarta.mail.MessagingException;

public interface EmailSender {

    void send(String to,String email) throws MessagingException;
}

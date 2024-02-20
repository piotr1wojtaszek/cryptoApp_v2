package com.piwo.cryptoApp.service;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class MailServiceTest {
    @Mock
    private JavaMailSender javaMailSender;
    @InjectMocks
    private MailService mailService;

    @Test
    void send_sendsNotifyToUser() throws MessagingException {
        //given
        String email = "user@email.com";
        String text = "random text";
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        //when
        mailService.send(email, text);
        //then
        verify(javaMailSender).send(mimeMessage);
        assertThat(mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString(), equalTo(email));
    }

    @Test
    public void send_throwsMessagingException(){
        //given
        String email = "user@email.com";
        String text = "random text";
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doAnswer(invocation -> {
            throw new MessagingException("Failed to send email");
        }).when(javaMailSender).send(mimeMessage);
        //when/then
        assertThrows(IllegalStateException.class, () -> mailService.send(email, text));
    }
}


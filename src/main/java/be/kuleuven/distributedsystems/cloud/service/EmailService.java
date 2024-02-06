package be.kuleuven.distributedsystems.cloud.service;

import be.kuleuven.distributedsystems.cloud.utils.Utils;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${spring.sendgrid.api-key}")
    private String sendGridApiKey;

    public void sendBookingConfirmationEmail(String toEmail, String subject, String body) {
        Email from = new Email(Utils.EMAIL_ADDRESS);
        Email to = new Email(toEmail);
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}


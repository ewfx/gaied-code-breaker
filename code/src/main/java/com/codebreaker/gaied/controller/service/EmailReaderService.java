package com.codebreaker.gaied.controller.service;

import com.codebreaker.gaied.entity.EmailRequest;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
public class EmailReaderService {

    private static final Tika tika = new Tika();
    @Value("${gaied_input_folder_path}")
    private String folderPath;


    public List<EmailRequest> getEmailRequests() {
        try {
            return Files.list(Paths.get(folderPath))
                    .filter(path -> path.toString().endsWith(".eml"))
                    .map(this::processEmlFile).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private EmailRequest processEmlFile(Path filePath) {
        EmailRequest emailRequest ;
        try (InputStream emlStream = Files.newInputStream(filePath)) {
            // Load the EML file as a MimeMessage
            Session session = Session.getDefaultInstance(new Properties());
            MimeMessage message = new MimeMessage(session, emlStream);

            // Extract email details
            String subject = message.getSubject();
            String body = extractBody(message);
            String attachmentText = extractAttachments(message);

            emailRequest  = EmailRequest.builder().emailName(filePath.getFileName().toString())
                    .emailSubject(subject)
                    .emailBody(body)
                    .attachmentText(attachmentText)
                    .build();
            return  emailRequest;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String extractBody(Part message) throws Exception {
        if (message.isMimeType("text/plain")) {
            return (String) message.getContent();
        } else if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    return (String) bodyPart.getContent();
                }
            }
        }
        return "(No text body found)";
    }

    public static String extractAttachments(Part message) throws Exception {
        if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            StringBuilder attachmentsText = new StringBuilder();

            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);

                // Check if it's an attachment (disposition or filename presence)
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) || bodyPart.getFileName() != null) {
                    String fileName = bodyPart.getFileName();
                    try (InputStream attachmentStream = new BufferedInputStream(bodyPart.getInputStream())) {
                        String extractedText = extractTextFromStream(attachmentStream, fileName);
                        attachmentsText.append("Attachment: ").append(fileName).append("\n")
                                .append(extractedText).append("\n\n");
                    }
                }
                // Recursively check nested attachments
                else if (bodyPart.isMimeType("multipart/*")) {
                    attachmentsText.append(extractAttachments(bodyPart));
                }
            }
            return attachmentsText.isEmpty() ? "(No attachments)" : attachmentsText.toString();
        }
        return "(No attachments)";
    }

    private static String extractTextFromStream(InputStream stream, String fileName) {
        try {
            BodyContentHandler handler = new BodyContentHandler(-1); // No size limit
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();

            // Use AutoDetectParser to automatically handle PDFs, DOCX, TXT, etc.
            new AutoDetectParser().parse(stream, handler, metadata, context);
            return handler.toString();
        } catch (IOException | TikaException | SAXException e) {
            return "Error extracting content: " + e.getMessage();
        }
    }

}

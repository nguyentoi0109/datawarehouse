package util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMail {
	public static void sendMail(String recipientEmail, String date, String title, String content) {
		// Thiết lập các thuộc tính cho kết nối email
		String host = "smtp.gmail.com"; // SMTP server của Gmail
		final String username = "nnnnn01092002@gmail.com"; // Thay thế bằng email của bạn
		final String password = "izxs urbo ervp mlxn"; // Thay thế bằng mật khẩu email của bạn
		
		Properties properties = new Properties();
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "587");

		// Tạo một session email
		Session session = Session.getInstance(properties, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {
			// Tạo một đối tượng MimeMessage
			Message message = new MimeMessage(session);

			// Thiết lập người gửi, người nhận và chủ đề
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
			message.setSubject(title);
			String emailContent = "Date: " + date + "\n\n" + content;
			message.setText(emailContent);

			// Gửi email
			Transport.send(message);

			System.out.println("Email sent successfully.");
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		LocalDate datenow = LocalDate.now();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
		String dateFormat = simpleDateFormat.format(datenow);
		SendMail.sendMail("nnnnn01092002@gmail.com", dateFormat, "error", "crawl error");
	}
}


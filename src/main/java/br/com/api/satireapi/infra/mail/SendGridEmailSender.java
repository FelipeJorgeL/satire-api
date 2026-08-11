package br.com.api.satireapi.infra.mail;

import br.com.api.satireapi.domain.customer.ConfirmationEmailSender;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
class SendGridEmailSender implements ConfirmationEmailSender {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

    private final RestClient restClient;
    private final String apiKey;
    private final String fromEmail;

    SendGridEmailSender(
        @Value("${app.mail.sendgrid-api-key:}") String apiKey,
        @Value("${app.mail.from:noreply@example.com}") String fromEmail
    ) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        this.restClient = RestClient.builder()
            .baseUrl("https://api.sendgrid.com/v3")
            .requestFactory(requestFactory)
            .build();
    }

    @Override
    public void sendEmailConfirmation(String to, String confirmationLink) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("SendGrid API key is not configured");
        }
        var body = Map.of(
            "personalizations", List.of(Map.of("to", List.of(Map.of("email", to)))),
            "from", Map.of("email", fromEmail, "name", "Satire"),
            "subject", "Confirme seu e-mail",
            "content", List.of(
                Map.of("type", "text/plain", "value", plainTextBody(confirmationLink)),
                Map.of("type", "text/html", "value", htmlBody(confirmationLink))
            ),
            "tracking_settings", Map.of(
                "click_tracking", Map.of("enable", false, "enable_text", false),
                "open_tracking", Map.of("enable", false)
            )
        );

        restClient.post()
            .uri("/mail/send")
            .header("Authorization", "Bearer " + apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .toBodilessEntity();
    }

    private static String plainTextBody(String confirmationLink) {
        return "Confirme seu cadastro na Satire clicando no link a seguir:\n"
            + confirmationLink
            + "\n\nEste link expira em 24 horas. Se você não se cadastrou na Satire, ignore este e-mail.";
    }

    private static String htmlBody(String confirmationLink) {
        return """
            <!doctype html>
            <html>
              <body>
                <h1>Confirme seu e-mail</h1>
                <p>Obrigado por se cadastrar na Satire.</p>
                <p><a href="%1$s">Confirmar e-mail</a></p>
                <p>Este link expira em 24 horas. Se você não se cadastrou na Satire, ignore este e-mail.</p>
              </body>
            </html>
            """.formatted(confirmationLink);
    }
}

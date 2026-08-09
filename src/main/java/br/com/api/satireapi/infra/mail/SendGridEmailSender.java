package br.com.api.satireapi.infra.mail;

import br.com.api.satireapi.domain.customer.internal.usecase.ConfirmationEmailSender;
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

    public SendGridEmailSender(
        @Value("${app.mail.sendgrid-api-key}") String apiKey,
        @Value("${app.mail.from}") String fromEmail
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

    // Nunca propaga falha de envio: um provedor de e-mail fora do ar não pode derrubar o registro,
    // que já foi persistido. A falha fica só no log do servidor.
    public void sendEmailConfirmation(String to, String confirmationLink) {
        var body = Map.of(
            "personalizations", List.of(Map.of("to", List.of(Map.of("email", to)))),
            "from", Map.of("email", fromEmail, "name", "Satire"),
            "subject", "Confirme seu e-mail",
            "content", List.of(
                Map.of("type", "text/plain", "value", plainTextBody(confirmationLink)),
                Map.of("type", "text/html", "value", htmlBody(confirmationLink))
            ),
            // Click/open tracking reescreve o link num redirecionador ct.sendgrid.net e injeta um
            // pixel invisível — além de esconder o link real, os dois pesam contra filtro de spam.
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
        return "Confirme seu cadastro na Satire clicando no link a seguir:\n" + confirmationLink
            + "\n\nEste link expira em 24 horas. Se você não se cadastrou na Satire, ignore este e-mail.";
    }

    private static String htmlBody(String confirmationLink) {
        return """
            <!doctype html>
            <html>
              <body style="font-family: Arial, sans-serif; background-color: #f4f4f5; padding: 24px; margin: 0;">
                <div style="max-width: 480px; margin: 0 auto; background: #ffffff; border-radius: 8px; padding: 32px;">
                  <h1 style="font-size: 20px; color: #111827; margin-top: 0;">Confirme seu e-mail</h1>
                  <p style="color: #374151; line-height: 1.5;">
                    Obrigado por se cadastrar na Satire. Clique no botão abaixo para confirmar seu e-mail e ativar sua conta.
                  </p>
                  <p style="text-align: center; margin: 32px 0;">
                    <a href="%1$s" style="background-color: #111827; color: #ffffff; padding: 12px 24px; border-radius: 6px; text-decoration: none; font-weight: bold; display: inline-block;">Confirmar e-mail</a>
                  </p>
                  <p style="color: #6b7280; font-size: 13px; word-break: break-all;">
                    Se o botão não funcionar, copie e cole este link no navegador:<br>
                    <a href="%1$s" style="color: #2563eb;">%1$s</a>
                  </p>
                  <p style="color: #9ca3af; font-size: 12px; margin-top: 24px;">
                    Este link expira em 24 horas. Se você não se cadastrou na Satire, ignore este e-mail.
                  </p>
                </div>
              </body>
            </html>
            """.formatted(confirmationLink);
    }
}

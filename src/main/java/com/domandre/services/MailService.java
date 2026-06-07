package com.domandre.services;

import com.domandre.exceptions.EmailIntegrationErrorException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${mail.confirmation.sender}")
    private String sender;

    @Value("${app.backend.url}")
    private String backendUrl;

    @Value("${app.frontend.url:}")
    private String frontendUrl;

    private String baseUrl() {
        return (frontendUrl != null && !frontendUrl.isBlank()) ? frontendUrl : backendUrl;
    }

    public void sendActivationEmail(String to, String token) {
        String link = (frontendUrl == null || frontendUrl.isBlank())
                ? backendUrl + "/api/auth/activate?token=" + token
                : frontendUrl + "/activate?token=" + token;

        sendEmail(to, "Ative a sua conta — DoctorApp", activationEmailHtml(link));
    }

    public void sendResetEmailPassword(String to, String token) {
        String link = (frontendUrl == null || frontendUrl.isBlank())
                ? backendUrl + "/api/auth/reset-password?token=" + token
                : frontendUrl + "/reset-password?token=" + token;

        sendEmail(to, "Redefinição de senha — DoctorApp", resetPasswordEmailHtml(link));
    }

    public void sendWelcomeEmail(String to, String firstName) {
        sendEmail(to, "Bem-vindo ao DoctorApp 🎉", welcomeEmailHtml(firstName, baseUrl()));
    }

    public void sendEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(sender);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailIntegrationErrorException();
        }
    }

    // ─── Email templates ─────────────────────────────────────────────────────

    private String activationEmailHtml(String link) {
        return emailWrapper(
            /* iconBg   */ "#dcfce7",
            /* iconColor*/ "#16a34a",
            /* iconChar */ "&#10003;",
            /* title    */ "Ative a sua conta",
            /* body     */ """
                Obrigado por se registar no DoctorApp.<br>
                Clique no bot&atilde;o abaixo para activar a sua conta e come&ccedil;ar a utilizar a plataforma.
                """,
            /* btnText  */ "Activar conta",
            /* btnLink  */ link,
            /* notice   */ "Este link expira em 24&nbsp;horas. Se n&atilde;o se registou no DoctorApp, pode ignorar este email com seguran&ccedil;a."
        );
    }

    private String resetPasswordEmailHtml(String link) {
        return emailWrapper(
            /* iconBg   */ "#dbeafe",
            /* iconColor*/ "#2563eb",
            /* iconChar */ "&#128274;",
            /* title    */ "Redefini&ccedil;&atilde;o de senha",
            /* body     */ """
                Recebemos um pedido de redefini&ccedil;&atilde;o da senha da sua conta DoctorApp.<br>
                Clique no bot&atilde;o abaixo para escolher uma nova senha.
                """,
            /* btnText  */ "Redefinir senha",
            /* btnLink  */ link,
            /* notice   */ "Este link expira em 1&nbsp;hora. Se n&atilde;o solicitou a redefini&ccedil;&atilde;o, ignore este email &mdash; a sua senha n&atilde;o ser&aacute; alterada."
        );
    }

    private String welcomeEmailHtml(String firstName, String loginUrl) {
        String body = """
                Ol&aacute;, %s!<br><br>
                A sua conta foi activada com sucesso. J&aacute; pode fazer login e come&ccedil;ar a utilizar todos os servi&ccedil;os da plataforma.
                """.formatted(firstName);

        return emailWrapper(
            /* iconBg   */ "#dcfce7",
            /* iconColor*/ "#16a34a",
            /* iconChar */ "&#10003;",
            /* title    */ "Bem-vindo ao DoctorApp!",
            /* body     */ body,
            /* btnText  */ "Entrar na plataforma",
            /* btnLink  */ loginUrl + "/login",
            /* notice   */ "Se recebeu este email por engano, pode ignor&aacute;-lo com seguran&ccedil;a."
        );
    }

    private String emailWrapper(
            String iconBg, String iconColor, String iconChar,
            String title, String body,
            String btnText, String btnLink,
            String notice
    ) {
        return """
            <!DOCTYPE html>
            <html lang="pt">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width,initial-scale=1.0">
              <title>%s</title>
            </head>
            <body style="margin:0;padding:0;background-color:#f9fafb;">
              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" bgcolor="#f9fafb" style="background-color:#f9fafb;">
                <tr>
                  <td align="center" style="padding:48px 16px;">

                    <!-- Brand -->
                    <table role="presentation" cellpadding="0" cellspacing="0" style="margin-bottom:28px;">
                      <tr>
                        <td align="center" bgcolor="#111827" style="background-color:#111827;border-radius:10px;padding:8px 18px;">
                          <span style="color:#ffffff;font-size:14px;font-weight:700;letter-spacing:0.5px;font-family:Arial,Helvetica,sans-serif;">DoctorApp</span>
                        </td>
                      </tr>
                    </table>

                    <!-- Card -->
                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:560px;background-color:#ffffff;border:1px solid #e5e7eb;border-radius:16px;overflow:hidden;">

                      <!-- Accent bar -->
                      <tr>
                        <td bgcolor="#111827" height="3" style="background-color:#111827;font-size:0;line-height:0;height:3px;">&nbsp;</td>
                      </tr>

                      <!-- Body -->
                      <tr>
                        <td style="padding:40px 40px 32px;">

                          <!-- Icon -->
                          <table role="presentation" cellpadding="0" cellspacing="0" style="margin:0 0 24px 0;">
                            <tr>
                              <td width="52" height="52" align="center"
                                  style="background-color:%s;border-radius:50%%;width:52px;height:52px;font-size:24px;line-height:52px;color:%s;font-family:Arial,Helvetica,sans-serif;">
                                %s
                              </td>
                            </tr>
                          </table>

                          <!-- Title -->
                          <h1 style="margin:0 0 12px;font-size:22px;font-weight:700;color:#111827;letter-spacing:-0.3px;font-family:Arial,Helvetica,sans-serif;">
                            %s
                          </h1>

                          <!-- Body text -->
                          <p style="margin:0 0 28px;font-size:15px;color:#6b7280;line-height:1.75;font-family:Arial,Helvetica,sans-serif;">
                            %s
                          </p>

                          <!-- CTA Button -->
                          <table role="presentation" cellpadding="0" cellspacing="0" style="margin-bottom:28px;">
                            <tr>
                              <td bgcolor="#111827" style="background-color:#111827;border-radius:10px;">
                                <a href="%s"
                                   style="display:inline-block;padding:13px 28px;font-size:14px;font-weight:600;color:#ffffff;text-decoration:none;border-radius:10px;font-family:Arial,Helvetica,sans-serif;">
                                  %s
                                </a>
                              </td>
                            </tr>
                          </table>

                          <!-- Fallback link -->
                          <p style="margin:0;font-size:12px;color:#9ca3af;line-height:1.6;font-family:Arial,Helvetica,sans-serif;">
                            Se o bot&atilde;o n&atilde;o funcionar, copie e cole este link no browser:<br>
                            <a href="%s" style="color:#9ca3af;word-break:break-all;text-decoration:underline;">%s</a>
                          </p>

                        </td>
                      </tr>

                      <!-- Footer -->
                      <tr>
                        <td style="padding:20px 40px;border-top:1px solid #f3f4f6;background-color:#f9fafb;">
                          <p style="margin:0;font-size:12px;color:#9ca3af;line-height:1.6;font-family:Arial,Helvetica,sans-serif;">
                            %s
                          </p>
                        </td>
                      </tr>

                    </table>

                    <!-- Copyright -->
                    <p style="margin:24px 0 0;font-size:12px;color:#9ca3af;font-family:Arial,Helvetica,sans-serif;">
                      &copy; 2025 DoctorApp &mdash; Todos os direitos reservados.
                    </p>

                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(
                title,          // <title>
                iconBg,         // icon circle background
                iconColor,      // icon character color
                iconChar,       // icon character
                title,          // h1
                body,           // body paragraph
                btnLink,        // CTA href
                btnText,        // CTA label
                btnLink,        // fallback href
                btnLink,        // fallback text
                notice          // footer notice
        );
    }
}

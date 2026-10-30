package com.mecai.car.services.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import com.mecai.car.services.EmailService;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {
    private static final String RUTA_PLANTILLAS = "templates/email/";

    @Value("${resend.key}")
    private String key;

    @Value("${resend.email}")
    private String resendEmail;

    @Override
    public void enviarBienvenida(String email, String password) {
        String html = renderizar("bienvenida.html", Map.of(
            "email", email,
            "password", password
        ));
        enviar(email, "Registro a Rodantex", html);
    }

    private void enviar(String destinatario, String asunto, String html) {
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Rodantex <" + resendEmail + ">")
                .to(destinatario)
                .subject(asunto)
                .html(html)
                .build();
        try {
            CreateEmailResponse data = new Resend(key).emails().send(params);
            log.info("Correo enviado a {} con id {}", destinatario, data.getId());
        } catch (ResendException e) {
            log.error("No se pudo enviar el correo a {}", destinatario, e);
        }
    }

    // Carga la plantilla y reemplaza cada {{variable}} por su valor escapado
    private String renderizar(String plantilla, Map<String, String> variables) {
        String html;
        try {
            html = new ClassPathResource(RUTA_PLANTILLAS + plantilla)
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la plantilla " + plantilla, e);
        }
        for (Map.Entry<String, String> variable : variables.entrySet()) {
            html = html.replace("{{" + variable.getKey() + "}}", HtmlUtils.htmlEscape(variable.getValue()));
        }
        return html;
    }
}

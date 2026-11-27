package br.edu.ifce.eventif.service;

import br.edu.ifce.eventif.entity.Enrollment;
import br.edu.ifce.eventif.entity.EventStatus;
import br.edu.ifce.eventif.entity.User;
import br.edu.ifce.eventif.repository.EnrollmentRepository;
import br.edu.ifce.eventif.repository.UserRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class CertificateService {
    private final EnrollmentRepository enrollments;
    private final UserRepository users;
    private final SpringTemplateEngine templates;

    public CertificateService(EnrollmentRepository enrollments, UserRepository users, SpringTemplateEngine templates) {
        this.enrollments = enrollments;
        this.users = users;
        this.templates = templates;
    }

    @Transactional(readOnly = true)
    public List<CertificateView> listCertificates(String email) {
        User user = users.findByEmail(email).filter(
                u -> u.isActive() && u.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_PARTICIPANTE")))
                .orElseThrow(() -> new AccessDeniedException("Apenas participantes podem acessar certificados."));
        Map<Long, CertificateView> certificates = new LinkedHashMap<>();
        for (Enrollment enrollment : enrollments.findByUserIdWithActivityAndEvent(user.getId())) {
            var activity = enrollment.getActivity();
            var event = activity.getEvent();
            if (!enrollment.isPresent() || event.getStatus() != EventStatus.CLOSED) {
                continue;
            }
            CertificateView previous = certificates.get(event.getId());
            long minutes = activity.getWorkloadMinutes() + (previous == null ? 0 : previous.workloadMinutes());
            certificates.put(event.getId(),
                    new CertificateView(event.getId(), user.getName(), event.getTitle(), minutes));
        }
        return List.copyOf(certificates.values());
    }

    @Transactional(readOnly = true)
    public CertificateView findCertificates(Long eventId, String email) {
        return listCertificates(email).stream().filter(certificate -> certificate.eventId().equals(eventId)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public byte[] generatePdf(Long eventId, String email) throws IOException {
        CertificateView certificate = findCertificates(eventId, email);
        Context context = new Context();
        context.setVariable("certificate", certificate);
        context.setVariable("pdf", true);
        String html = templates.process("certificados/certificado", context);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(html, null);
        builder.toStream(output);
        builder.run();
        return output.toByteArray();
    }

    public record CertificateView(Long eventId, String participantName, String eventTitle, long workloadMinutes) {
        public String formattedWorkload() {
            long hours = workloadMinutes / 60;
            long minutes = workloadMinutes % 60;
            if (minutes == 0)
                return hours + (hours == 1 ? " hora" : " horas");
            if (hours == 0)
                return minutes + (minutes == 1 ? " minuto" : " minutos");
            return hours + (hours == 1 ? " hora e " : " horas e ") + minutes + (minutes == 1 ? " minuto" : " minutos");
        }
    }
}

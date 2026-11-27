package br.edu.ifce.eventif.controller;

import br.edu.ifce.eventif.service.CertificateService;
import java.io.IOException;
import java.security.Principal;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/certificados")
public class CertificateController {
    private final CertificateService certificates;
    public CertificateController(CertificateService certificates) {
        this.certificates = certificates;
    }

    @GetMapping
    public String list(Principal principal, Model model) {
        model.addAttribute("certificates", certificates.listMine(principal.getName()));
        return "certificados/lista";
    }

    @GetMapping("/{eventId}")
    public String show(@PathVariable Long eventId, Principal principal, Model model) {
        model.addAttribute("certificate", certificates.findMine(eventId, principal.getName()));
        model.addAttribute("pdf", false);
        return "certificados/certificado";
    }

    @GetMapping("/{eventId}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long eventId, Principal principal) throws IOException {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename("certificado-" + principal.getName() + ".pdf").build().toString())
                .cacheControl(org.springframework.http.CacheControl.noStore())
                .body(certificates.generatePdf(eventId, principal.getName()));
    }
}

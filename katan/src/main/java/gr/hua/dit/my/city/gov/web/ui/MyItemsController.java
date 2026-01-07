package gr.hua.dit.my.city.gov.web.ui;

import gr.hua.dit.my.city.gov.core.model.Appointment;
import gr.hua.dit.my.city.gov.core.model.Issue;
import gr.hua.dit.my.city.gov.core.model.Request;
import gr.hua.dit.my.city.gov.core.repository.AppointmentRepository;
import gr.hua.dit.my.city.gov.core.repository.IssueRepository;
import gr.hua.dit.my.city.gov.core.repository.RequestRepository;
import gr.hua.dit.my.city.gov.core.security.CurrentUser;
import gr.hua.dit.my.city.gov.core.security.CurrentUserProvider;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Controller
public class MyItemsController {

    private final RequestRepository requestRepository;
    private final AppointmentRepository appointmentRepository;
    private final IssueRepository issueRepository;
    private final CurrentUserProvider currentUserProvider;
    private final MinioClient minioClient;
    private final String minioBucket;

    public MyItemsController(RequestRepository requestRepository,
                             AppointmentRepository appointmentRepository,
                             IssueRepository issueRepository,
                             CurrentUserProvider currentUserProvider,
                             MinioClient minioClient,
                             @Value("${app.minio.bucket}") String minioBucket) {
        this.requestRepository = requestRepository;
        this.appointmentRepository = appointmentRepository;
        this.issueRepository = issueRepository;
        this.currentUserProvider = currentUserProvider;
        this.minioClient = minioClient;
        this.minioBucket = minioBucket;
    }

    private Long requireCurrentPersonId() {
        return currentUserProvider.getCurrentUser()
                .map(CurrentUser::id)
                .orElse(null);
    }

    // My Requests
    @GetMapping("/my/requests")
    public String myRequests(Model model) {
        Long personId = requireCurrentPersonId();
        if (personId == null) {
            return "redirect:/login";
        }
        List<Request> requests = requestRepository.findByPersonId(personId);
        model.addAttribute("requests", requests);
        return "my-requests-list :: content";
    }

    @GetMapping("/my/requests/{id}")
    public String myRequestDetail(@PathVariable Long id, Model model) {
        Long personId = requireCurrentPersonId();
        if (personId == null) {
            return "redirect:/login";
        }
        Request request = requestRepository.findById(id).orElse(null);
        if (request == null || request.getPersonId() == null || !personId.equals(request.getPersonId())) {
            return "redirect:/my/requests";
        }

        List<String> attachments = Collections.emptyList();
        if (request.getAttachmentKey() != null && !request.getAttachmentKey().isBlank()) {
            attachments = Arrays.asList(request.getAttachmentKey().split(","));
        }

        model.addAttribute("request", request);
        model.addAttribute("attachments", attachments);
        return "my-request-detail :: content";
    }

    // My Appointments
    @GetMapping("/my/appointments")
    public String myAppointments(Model model) {
        Long personId = requireCurrentPersonId();
        if (personId == null) {
            return "redirect:/login";
        }
        List<Appointment> appointments = appointmentRepository.findByPersonId(personId);
        model.addAttribute("appointments", appointments);
        return "my-appointments-list :: content";
    }

    @GetMapping("/my/appointments/{id}")
    public String myAppointmentDetail(@PathVariable Long id, Model model) {
        Long personId = requireCurrentPersonId();
        if (personId == null) {
            return "redirect:/login";
        }
        Appointment appointment = appointmentRepository.findById(id).orElse(null);
        if (appointment == null || appointment.getPersonId() == null || !personId.equals(appointment.getPersonId())) {
            return "redirect:/my/appointments";
        }
        model.addAttribute("appointment", appointment);
        return "my-appointment-detail :: content";
    }

    // My Issues
    @GetMapping("/my/issues")
    public String myIssues(Model model) {
        Long personId = requireCurrentPersonId();
        if (personId == null) {
            return "redirect:/login";
        }
        List<Issue> issues = issueRepository.findByPersonId(personId);
        model.addAttribute("issues", issues);
        return "my-issues-list :: content";
    }

    @GetMapping("/my/issues/{id}")
    public String myIssueDetail(@PathVariable Long id, Model model) {
        Long personId = requireCurrentPersonId();
        if (personId == null) {
            return "redirect:/login";
        }
        Issue issue = issueRepository.findById(id).orElse(null);
        if (issue == null || issue.getPersonId() == null || !personId.equals(issue.getPersonId())) {
            return "redirect:/my/issues";
        }
        model.addAttribute("issue", issue);
        return "my-issue-detail :: content";
    }

    // Download a specific attachment for a request (index is zero-based)
    @GetMapping("/my/requests/{id}/attachments/{index}")
    public ResponseEntity<InputStreamResource> downloadRequestAttachment(@PathVariable Long id,
                                                                         @PathVariable int index) throws Exception {
        Long personId = requireCurrentPersonId();
        if (personId == null) {
            return ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        Request request = requestRepository.findById(id).orElse(null);
        if (request == null || request.getPersonId() == null || !personId.equals(request.getPersonId())) {
            return ResponseEntity.status(403).build();
        }

        String keyString = request.getAttachmentKey();
        if (keyString == null || keyString.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        String[] keys = keyString.split(",");
        if (index < 0 || index >= keys.length) {
            return ResponseEntity.notFound().build();
        }

        String objectName = keys[index].trim();
        if (objectName.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String originalName = objectName;
        int dashIndex = originalName.lastIndexOf('-');
        if (dashIndex >= 0 && dashIndex < originalName.length() - 1) {
            originalName = originalName.substring(dashIndex + 1);
        }

        InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioBucket)
                        .object(objectName)
                        .build()
        );

        String encodedFileName = URLEncoder.encode(originalName, StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(stream));
    }
}

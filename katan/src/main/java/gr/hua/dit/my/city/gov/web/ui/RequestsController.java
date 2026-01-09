package gr.hua.dit.my.city.gov.web.ui;

import gr.hua.dit.my.city.gov.core.model.Person;
import gr.hua.dit.my.city.gov.core.model.RequestType;
import gr.hua.dit.my.city.gov.core.repository.PersonRepository;
import gr.hua.dit.my.city.gov.core.model.Request;
import gr.hua.dit.my.city.gov.core.repository.RequestRepository;
import gr.hua.dit.my.city.gov.core.repository.RequestTypeRepository;
import gr.hua.dit.my.city.gov.core.security.CurrentUser;
import gr.hua.dit.my.city.gov.core.security.CurrentUserProvider;
import gr.hua.dit.my.city.gov.core.service.MinioStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Controller
public class RequestsController {

    private final RequestRepository requestRepository;
    private final RequestTypeRepository requestTypeRepository;
    private final PersonRepository personRepository;

    @Autowired
    private MinioStorageService minioStorageService;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    public RequestsController(
            RequestRepository requestRepository,
            RequestTypeRepository requestTypeRepository,
            PersonRepository personRepository
    ) {
        this.requestRepository = requestRepository;
        this.requestTypeRepository = requestTypeRepository;
        this.personRepository = personRepository;
    }

    @GetMapping("/requests/form")
    public String form(Model model) {
        model.addAttribute("request", new Request());
        model.addAttribute("requestTypes", requestTypeRepository.findByActiveTrue());
        return "requests-form :: content";
    }

    @GetMapping("/requests/my")
    public String myRequests(Model model, Authentication authentication) {
        Person citizen = resolveCurrentPerson(authentication);

        model.addAttribute("requests", requestRepository.findByCitizenId(citizen.getId()));
        return "requests-list :: content";
    }

    @PostMapping("/requests")
    public String saveRequest(Request request,
                             @RequestParam(value = "requestTypeId", required = false) Long requestTypeId,
                             @RequestParam(value = "attachments", required = false) MultipartFile[] attachments, Authentication authentication) throws Exception {

        // associate with current user, if logged in
        currentUserProvider.getCurrentUser()
            .map(CurrentUser::id)
            .ifPresent(request::setPersonId);

        if (requestTypeId != null) {
            RequestType requestType = requestTypeRepository.findById(requestTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid requestTypeId: " + requestTypeId));
            request.setRequestType(requestType);
        } else {
            throw new IllegalArgumentException("Request type is required");
        }

        if (attachments != null && attachments.length > 0) {
            List<String> keys = new ArrayList<>();
            for (MultipartFile attachment : attachments) {
                if (attachment != null && !attachment.isEmpty()) {
                    String objectName = minioStorageService.upload(attachment);
                    if (objectName != null) {
                        keys.add(objectName);
                    }
                }
            }
            if (!keys.isEmpty()) {
                request.setAttachmentKey(String.join(",", keys));
            }
        }

        Person person = resolveCurrentPerson(authentication);
        request.setCitizen(person);

        requestRepository.save(request);
        return "requests-success :: content";
    }

    private Person resolveCurrentPerson(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("No authenticated user found");
        }

        String email = authentication.getName();

        return personRepository
                .findByEmailAddressIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Person not found for email: " + email));
    }
}


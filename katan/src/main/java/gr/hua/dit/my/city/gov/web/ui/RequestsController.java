package gr.hua.dit.my.city.gov.web.ui;

import gr.hua.dit.my.city.gov.core.model.Request;
import gr.hua.dit.my.city.gov.core.repository.RequestRepository;
import gr.hua.dit.my.city.gov.core.security.CurrentUser;
import gr.hua.dit.my.city.gov.core.security.CurrentUserProvider;
import gr.hua.dit.my.city.gov.core.service.MinioStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Controller
public class RequestsController {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private MinioStorageService minioStorageService;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    @GetMapping("/requests/form")
    public String showRequestsForm() {
        return "requests-form :: content";
    }

    @PostMapping("/requests")
    public String saveRequest(Request request,
                             @RequestParam(value = "attachments", required = false) MultipartFile[] attachments) throws Exception {

        // associate with current user, if logged in
        currentUserProvider.getCurrentUser()
            .map(CurrentUser::id)
            .ifPresent(request::setPersonId);

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

        requestRepository.save(request);
        return "requests-success :: content";
    }
}


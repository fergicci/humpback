package studio.humpback.backend.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import studio.humpback.backend.dto.ApiResponse;
import studio.humpback.backend.dto.ContactRequest;
import studio.humpback.backend.model.Contact;
import studio.humpback.backend.repository.ContactRepository;

@RestController
@RequestMapping("/api/v1/contact")
public class ContactController {

    private final ContactRepository contactRepository;

    public ContactController(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @PostMapping
    public ApiResponse<Contact> submitContact(@RequestBody @Valid ContactRequest contactRequest) {
        Contact contact = Contact.builder()
                .name(contactRequest.getName())
                .email(contactRequest.getEmail())
                .telephone(contactRequest.getTelephone())
                .message(contactRequest.getMessage())
                .build();

        contactRepository.save(contact);

        return ApiResponse.success();
    }
}
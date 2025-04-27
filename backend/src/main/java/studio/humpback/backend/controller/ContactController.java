package studio.humpback.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import studio.humpback.backend.dto.ApiResponse;
import studio.humpback.backend.dto.ContactRequest;
import studio.humpback.backend.dto.ContactResponse;
import studio.humpback.backend.model.Contact;
import studio.humpback.backend.service.ContactService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/contacts")
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ApiResponse<ContactResponse> createContact(@RequestBody @Valid ContactRequest request) {
        Contact contact = contactService.create(request);
        return ApiResponse.success(toResponse(contact));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<List<ContactResponse>> getContacts() {
        List<Contact> contacts = contactService.findAll();
        List<ContactResponse> response = contacts.stream()
                .map(this::toResponse)
                .toList();
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> deleteContact(@PathVariable String id) {
        contactService.deleteById(id);
        return ApiResponse.success();
    }

    private ContactResponse toResponse(Contact contact) {
        return ContactResponse.builder()
                .id(contact.getId())
                .name(contact.getName())
                .email(contact.getEmail())
                .telephone(contact.getTelephone())
                .message(contact.getMessage())
                .createdAt(contact.getCreatedAt())
                .build();
    }
}

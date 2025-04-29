package studio.humpback.backend.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;

import studio.humpback.backend.dto.ApiResponse;
import studio.humpback.backend.dto.PagedResponse;
import studio.humpback.backend.dto.ContactRequest;
import studio.humpback.backend.dto.ContactResponse;
import studio.humpback.backend.model.Contact;
import studio.humpback.backend.service.ContactService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ApiResponse<ContactResponse> createContact(@RequestBody @Valid ContactRequest request) {
        Contact contact = contactService.create(request);
        return ApiResponse.success(toResponse(contact));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<PagedResponse<ContactResponse>> getContacts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(
            page - 1,
            size,
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Page<Contact> contactsPage = contactService.getPage(pageable);

        List<ContactResponse> content = contactsPage.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        PagedResponse<ContactResponse> pagedResponse = PagedResponse.<ContactResponse>builder()
                .content(content)
                .page(contactsPage.getNumber())
                .size(contactsPage.getSize())
                .totalElements(contactsPage.getTotalElements())
                .totalPages(contactsPage.getTotalPages())
                .build();

        return ApiResponse.success(pagedResponse);
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
                .read(contact.getRead())
                .build();
    }
}

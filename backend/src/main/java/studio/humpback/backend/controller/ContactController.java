package studio.humpback.backend.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import studio.humpback.backend.dto.ApiResponse;
import studio.humpback.backend.dto.ContactRequest;
import studio.humpback.backend.dto.ContactResponse;
import studio.humpback.backend.dto.PagedResponse;
import studio.humpback.backend.model.Contact;
import studio.humpback.backend.service.ContactService;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public ApiResponse<ContactResponse> createContact(@RequestBody @Valid ContactRequest request) {
        Contact contact = contactService.create(request);
        return ApiResponse.success(toResponse(contact));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'READER')")
    public ApiResponse<PagedResponse<ContactResponse>> getContacts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
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
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteContact(@PathVariable String id) {
        contactService.delete(id);
    }

    @PatchMapping("/{id}/read/{desiredRead}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Object> setContactRead(@PathVariable String id, @PathVariable Boolean desiredRead) {
        contactService.setRead(id, desiredRead);
        return ApiResponse.success();
    }

    private ContactResponse toResponse(Contact contact) {
        return ContactResponse.builder()
                .id(contact.getId())
                .name(contact.getName())
                .email(contact.getEmail())
                .message(contact.getMessage())
                .createdAt(contact.getCreatedAt())
                .read(contact.getRead())
                .build();
    }
}

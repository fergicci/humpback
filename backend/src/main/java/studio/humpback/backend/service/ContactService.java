package studio.humpback.backend.service;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import studio.humpback.backend.dto.ContactRequest;
import studio.humpback.backend.exception.ResourceNotFoundException;
import studio.humpback.backend.model.Contact;
import studio.humpback.backend.repository.ContactRepository;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final static String CONTACT_ID_NOT_FOUND = "Contact %s not found!";

    private final ContactRepository contactRepository;

    public Contact create(ContactRequest request) {
        Contact contact = Contact.builder()
                .name(request.getName())
                .email(request.getEmail())
                .message(request.getMessage())
                .createdAt(Instant.now())
                .build();
        return contactRepository.save(contact);
    }

    public List<Contact> findAll() {
        return contactRepository.findAll();
    }

    public Page<Contact> getPage(Pageable pageable) {
        return contactRepository.findAll(pageable);
    }

    public void delete(String id) {
        contactRepository.deleteById(id);
    }

    public Contact getById(String id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(CONTACT_ID_NOT_FOUND, id)));
    }

    public void setRead(String id, Boolean desiredRead) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(CONTACT_ID_NOT_FOUND, id)));

        if (contact.getRead().equals(desiredRead))
            return;

        contact.setRead(desiredRead);
        contactRepository.save(contact);
    }
}

package studio.humpback.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import studio.humpback.backend.model.Contact;
import studio.humpback.backend.repository.ContactRepository;
import studio.humpback.backend.dto.ContactRequest;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;

    public Contact create(ContactRequest request) {
        Contact contact = Contact.builder()
                .name(request.getName())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .message(request.getMessage())
                .createdAt(Instant.now())
                .build();
        return contactRepository.save(contact);
    }

    public List<Contact> findAll() {
        return contactRepository.findAll();
    }

    public void deleteById(String id) {
        contactRepository.deleteById(id);
    }

    public Contact getById(String id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact not found"));
    }
}
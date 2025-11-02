package studio.humpback.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import studio.humpback.backend.model.Contact;

public interface ContactRepository extends MongoRepository<Contact, String> {
}

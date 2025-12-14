package studio.humpback.backend.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import studio.humpback.backend.model.Booking;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {

    public List<Booking> findByBookingAtBetween(Instant startDateTime, Instant endDateTime);
    public List<Booking> findByBookingAtLessThanAndEndAtGreaterThan(Instant end, Instant start);
    public List<Booking> findByEmail(String email);
    
}

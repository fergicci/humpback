package studio.humpback.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import studio.humpback.backend.model.DashboardSnapshot;

@Repository
public interface DashboardSnapshotRepository extends MongoRepository<DashboardSnapshot, String> {
}

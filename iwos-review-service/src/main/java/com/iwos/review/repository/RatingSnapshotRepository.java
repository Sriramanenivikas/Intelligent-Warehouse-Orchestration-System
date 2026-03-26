package com.iwos.review.repository;

import com.iwos.review.entity.RatingSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingSnapshotRepository extends MongoRepository<RatingSnapshot, String> {
}

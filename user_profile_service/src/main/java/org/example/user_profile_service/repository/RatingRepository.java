package org.example.user_profile_service.repository;

import org.example.user_profile_service.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {
}

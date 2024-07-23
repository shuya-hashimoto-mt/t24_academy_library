package jp.co.metateam.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import jp.co.metateam.library.model.Review;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long>{

    @Query(value = "SELECT * FROM review WHERE book_id = ?1", nativeQuery = true)
    List<Review> findReviewById(Long book_id);
}

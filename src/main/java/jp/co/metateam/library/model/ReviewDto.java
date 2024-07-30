package jp.co.metateam.library.model;

import java.security.Timestamp;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * レビューDto
 */
@Getter
@Setter
public class ReviewDto {

    private int id;
    
    private Long book_id;

    @NotNull(message = "評価は必須です")
    private Integer score;

    @NotEmpty(message = "書籍レビューは必須です")
    private String body;

    private BookMst bookMst;
}

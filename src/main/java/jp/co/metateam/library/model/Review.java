package jp.co.metateam.library.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;





/**
 * レビュー
 */

@Entity
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

     /** 登録日時 */
    @Column(name = "created_at", nullable = false)
    private Timestamp created_at;

    /** 評価点数 */
    @Column(name = "score ", nullable = false)
    private Integer score;

    /** レビュー本文 */
    @Column(name = "body", nullable = false)
    private String body;

    /** BookMst外部キー */
    @ManyToOne
    @JoinColumn(name = "book_id", referencedColumnName = "id", nullable = false)
    private BookMst bookMst;

    /** Getter */

    public Long getId(){
        return id;
    }

    public Timestamp getCreatedat(){
        return created_at;
    }

    public int getScore(){
        return score;
    }

    public String body(){
        return body;
    }

    public BookMst getBookMst() {
        return bookMst;
    }

    /** Setter */

    public void setCreatedat(Timestamp created_at){
        this.created_at = created_at;
    }

    public void setScore(int score){
        this.score = score;
    }

    public void setBody(String body){
        this.body = body;
    }

    public void setBookMst(BookMst bookMst) {
        this.bookMst = bookMst;
    }
}

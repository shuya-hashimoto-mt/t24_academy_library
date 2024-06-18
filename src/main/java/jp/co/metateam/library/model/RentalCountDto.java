package jp.co.metateam.library.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 貸出可能数DTO
 */
@Getter
@Setter
public class RentalCountDto {

    /**貸出可能数 */
    private Object rentalCount;

    /** 在庫管理番号 */
    public List<String> idList;{
        this.idList = new ArrayList<>();
    }
    
    /**貸出予定日 */
    private Date expectedRentalOn;
}

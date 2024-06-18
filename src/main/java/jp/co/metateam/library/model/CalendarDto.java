package jp.co.metateam.library.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * カレンダーDTO
 */
@Getter
@Setter
public class CalendarDto {

    /**書籍タイトル */
    private String title;

    /**利用可能在庫数 */
    private int stockCount;

    /**貸出可能数 */
    public List<RentalCountDto> rentalCountList;{
         this.rentalCountList = new ArrayList<>();
    }
}

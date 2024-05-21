package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import jp.co.metateam.library.values.RentalStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;



/**
 * 貸出管理DTO
 */
@Getter
@Setter
public class RentalManageDto {

    private Long id;

    @NotEmpty(message="在庫管理番号は必須です")
    private String stockId;

    @NotEmpty(message="社員番号は必須です")
    private String employeeId;

    @NotNull(message="貸出ステータスは必須です")
    private Integer status;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="貸出予定日は必須です")
    private Date expectedRentalOn;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="返却予定日は必須です")
    private Date expectedReturnOn;

    private Timestamp rentaledAt;

    private Timestamp returnedAt;

    private Timestamp canceledAt;

    private Stock stock;

    private Account account;


/**
 * 貸出ステータスチェック
 * 
 * @param previousRentalStatus 変更前 貸出ステータス
 * @return エラーメッセージ
 */
public Optional<String> validStatus(Integer previousRentalStatus){
    String errFormat = "貸出ステータスを%sから%sに編集することはできません";
    String errDate = "貸出予定日は現在の日付を入力してください";
    String instruction = "在庫を貸出したい場合は、新規で貸出登録をしてください";

    //貸出待ち→貸出中かつ貸出予定日! = 現在日付 or 貸出待ち→返却済みの場合にエラー
    if(previousRentalStatus == RentalStatus.RENT_WAIT.getValue() && previousRentalStatus != this.status){
        if(this.status == RentalStatus.RENTAlING.getValue() && !isRentalDateValid()){
            return Optional.of(errDate);
       }else if(this.status == RentalStatus.RETURNED.getValue()){
            return Optional.of(String.format(errFormat,RentalStatus.RENT_WAIT.getText(),RentalStatus.RETURNED.getText()));
        }
    }


    //貸出中→貸出待ち or 貸出中→キャンセルの場合にエラー
    else if(previousRentalStatus == RentalStatus.RENTAlING.getValue()){
        if(this.status == RentalStatus.RENT_WAIT.getValue()){
            return Optional.of(String.format(errFormat,RentalStatus.RENTAlING.getText(),RentalStatus.RENT_WAIT.getText()));
        }else if(this.status == RentalStatus.CANCELED.getValue()){
            return Optional.of(String.format(errFormat,RentalStatus.RENTAlING.getText(),RentalStatus.CANCELED.getText()));
        }
    }

    //返却済み→貸出待ち or 返却済み→貸出中 or 返却済み→キャンセルの場合にエラー
    else if(previousRentalStatus == RentalStatus.RETURNED.getValue()){
        if(this.status == RentalStatus.RENT_WAIT.getValue()){
            return Optional.of(String.format(errFormat,RentalStatus.RETURNED.getText(),RentalStatus.RENT_WAIT.getText()));
        }else if(this.status == RentalStatus.RENTAlING.getValue()){
            return Optional.of(String.format(errFormat,RentalStatus.RETURNED.getText(),RentalStatus.RENTAlING.getText()));
        }else if(this.status == RentalStatus.CANCELED.getValue()){
            return Optional.of(String.format(errFormat,RentalStatus.RETURNED.getText(),RentalStatus.CANCELED.getText()));
        }
    }

    //キャンセル→貸出待ち or キャンセル→貸出中 or キャンセル→返却済みの場合にエラー
    else if(previousRentalStatus == RentalStatus.CANCELED.getValue()){
        if(this.status == RentalStatus.RENT_WAIT.getValue()){
            return Optional.of(String.format(errFormat,RentalStatus.CANCELED.getText(),RentalStatus.RENT_WAIT.getText()));
        }else if(this.status == RentalStatus.RENTAlING.getValue()){
            return Optional.of(String.format(errFormat,RentalStatus.CANCELED.getText(),RentalStatus.RENTAlING.getText()));
        }else if(this.status == RentalStatus.CANCELED.getValue()){
            return Optional.of(String.format(errFormat,RentalStatus.CANCELED.getText(),RentalStatus.CANCELED.getText()));
        }
    }

    //変更前のステータスがnullの場合にエラー
    else if(previousRentalStatus == null){
        return Optional.of(instruction);
    }

    return Optional.empty();
}
public boolean isRentalDateValid(){
    LocalDate localDate = LocalDate.now();
    Instant instant = this.expectedRentalOn.toInstant();
   // Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    LocalDate expectedRentalOn = instant.atZone(ZoneId.systemDefault()).toLocalDate();

    if(localDate.isEqual(expectedRentalOn)){
        return true;
    }
    return false;  
}

}

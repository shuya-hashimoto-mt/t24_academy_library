package jp.co.metateam.library.service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.repository.AccountRepository;
import jp.co.metateam.library.repository.RentalManageRepository;
import jp.co.metateam.library.repository.StockRepository;
import jp.co.metateam.library.values.RentalStatus;

@Service
public class RentalManageService {

    private final AccountRepository accountRepository;
    private final RentalManageRepository rentalManageRepository;
    private final StockRepository stockRepository;

     @Autowired
    public RentalManageService(
        AccountRepository accountRepository,
        RentalManageRepository rentalManageRepository,
        StockRepository stockRepository
    ) {
        this.accountRepository = accountRepository;
        this.rentalManageRepository = rentalManageRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public List <RentalManage> findAll() {
        List <RentalManage> rentalManageList = this.rentalManageRepository.findAll();

        return rentalManageList;
    }

    @Transactional
    public RentalManage findById(Long id) {
        return this.rentalManageRepository.findById(id).orElse(null);
    }

    @Transactional
    public long countByStockIdAndStatusIn(String stock_id){
        return this.rentalManageRepository.countByStockIdAndStatusIn(stock_id);
    }

    @Transactional
    public long countByStockIdAndStatusInAndIdNot(String stockId, Long Id){
        return this.rentalManageRepository.countByStockIdAndStatusInAndIdNot(stockId, Id);
    }

    @Transactional
    public long countByStockIdAndStatusAndtermsIn(String stockId, Date expectedReturnOn, Date expectedRentalOn){
        return this.rentalManageRepository.countByStockIdAndStatusAndTermsIn(stockId, expectedReturnOn, expectedRentalOn);
    }

    @Transactional
    public long countByStockIdAndStatusAndIdNotAndTermsIn(String stockId, Long id , Date expectedReturnOn, Date expectedRentalOn) {
        return this.rentalManageRepository.countByStockIdAndStatusAndIdNotAndTermsIn(stockId, id, expectedReturnOn, expectedRentalOn);
    }



    @Transactional 
    public void save(RentalManageDto rentalManageDto) throws Exception {
        try {
            Account account = this.accountRepository.findByEmployeeId(rentalManageDto.getEmployeeId()).orElse(null);
            if (account == null) {
                throw new Exception("Account not found.");
            }

            Stock stock = this.stockRepository.findById(rentalManageDto.getStockId()).orElse(null);
            if (stock == null) {
                throw new Exception("Stock not found.");
            }

            RentalManage rentalManage = new RentalManage();
            rentalManage = setRentalStatusDate(rentalManage, rentalManageDto.getStatus());

            rentalManage.setAccount(account);
            rentalManage.setExpectedRentalOn(rentalManageDto.getExpectedRentalOn());
            rentalManage.setExpectedReturnOn(rentalManageDto.getExpectedReturnOn());
            rentalManage.setStatus(rentalManageDto.getStatus());
            rentalManage.setStock(stock);

            // データベースへの保存
            this.rentalManageRepository.save(rentalManage);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional 
    public void update(long id, RentalManageDto rentalManageDto) throws Exception {
        try {
            //システム動作要確認
            RentalManage rentalManage = findById(id);                                     
            if (rentalManage == null) {
                throw new Exception("RentalManage record not found.");
            }

            Stock stock = this.stockRepository.findById(rentalManageDto.getStockId()).orElse(null);
            if (stock == null) {
                throw new Exception("Stock record not found.");
            }

            Account account = this.accountRepository.findByEmployeeId(rentalManageDto.getEmployeeId()).orElse(null);
            if(account == null) { 
                throw new Exception("Account record not found.");
            }
            //ここまで

            rentalManage = setRentalStatusDate(rentalManage, rentalManageDto.getStatus());

            //rentalManage.setId(rentalManageDto.getId());       //貸出管理番号に変更が加わることはあるのか
            rentalManage.setAccount(account);
            rentalManage.setExpectedRentalOn(rentalManageDto.getExpectedRentalOn());
            rentalManage.setExpectedReturnOn(rentalManageDto.getExpectedReturnOn());
            rentalManage.setStock(stock);  
            rentalManage.setStatus(rentalManageDto.getStatus());
                       


            // データベースへの保存
            this.rentalManageRepository.save(rentalManage);
        }catch (Exception e) {
            throw e;
        }
    }

    private RentalManage setRentalStatusDate(RentalManage rentalManage, Integer status) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        
        if (status == RentalStatus.RENTAlING.getValue()) {
            rentalManage.setRentaledAt(timestamp);
        } else if (status == RentalStatus.RETURNED.getValue()) {
            rentalManage.setReturnedAt(timestamp);
        } else if (status == RentalStatus.CANCELED.getValue()) {
            rentalManage.setCanceledAt(timestamp);
        }

        return rentalManage;
    }
}


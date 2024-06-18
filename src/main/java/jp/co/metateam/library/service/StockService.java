package jp.co.metateam.library.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.constants.Constants;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.model.CalendarDto;
import jp.co.metateam.library.model.RentalCountDto;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.repository.BookMstRepository;
import jp.co.metateam.library.repository.StockRepository;
import jp.co.metateam.library.repository.RentalManageRepository;

import jp.co.metateam.library.values.RentalStatus;

@Service
public class StockService {
    private final BookMstRepository bookMstRepository;
    private final StockRepository stockRepository;
    private final RentalManageRepository rentalManageRepository;

    @Autowired
    public StockService(BookMstRepository bookMstRepository, StockRepository stockRepository, RentalManageRepository rentalManageRepository){
        this.bookMstRepository = bookMstRepository;
        this.stockRepository = stockRepository;
        this.rentalManageRepository = rentalManageRepository;
    }

    @Transactional
    public List<Stock> findAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNull();

        return stocks;
    }
    
    @Transactional
    public List <Stock> findStockAvailableAll() {
        List <Stock> stocks = this.stockRepository.findByDeletedAtIsNullAndStatus(Constants.STOCK_AVAILABLE);

        return stocks;
    }

    @Transactional
    public Stock findById(String id) {
        return this.stockRepository.findById(id).orElse(null);
    }

    @Transactional 
    public void save(StockDto stockDto) throws Exception {
        try {
            Stock stock = new Stock();
            BookMst bookMst = this.bookMstRepository.findById(stockDto.getBookId()).orElse(null);
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setBookMst(bookMst);
            stock.setId(stockDto.getId());
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional 
    public void update(String id, StockDto stockDto) throws Exception {
        try {
            Stock stock = findById(id);
            if (stock == null) {
                throw new Exception("Stock record not found.");
            }

            BookMst bookMst = stock.getBookMst();
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setId(stockDto.getId());
            stock.setBookMst(bookMst);
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    //曜日の取得
    public List<Object> generateDaysOfWeek(int year, int month, LocalDate startDate, int daysInMonth) {
        List<Object> daysOfWeek = new ArrayList<>();
        for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
            LocalDate date = LocalDate.of(year, month, dayOfMonth);
            DateTimeFormatter formmater = DateTimeFormatter.ofPattern("dd(E)", Locale.JAPANESE);
            daysOfWeek.add(date.format(formmater));
        }

        return daysOfWeek;
    }

   

    public List <CalendarDto> generateValues(Integer year, Integer month, Integer daysInMonth) {      
        List <CalendarDto> values = new ArrayList<>();

        List <BookMst> bookMstList = bookMstRepository.findAll(); 
        for(int i = 0; i < bookMstList.size(); i++){
            BookMst book = bookMstList.get(i);
            List <Stock> stockList = stockRepository.findByBookMstIdAndStatus(book.getId(), Constants.STOCK_AVAILABLE);

            if(stockList.size() > 0){
                CalendarDto calendarDto = new CalendarDto();
                calendarDto.setTitle(book.getTitle());
                calendarDto.setStockCount(stockList.size());

                LocalDate currentDate = LocalDate.now();

                for(int d = 1; d <= daysInMonth; d++){
                    LocalDate dateCheck = LocalDate.of(year, month, d);  
                    
                    calendarDto.rentalCountList.add(rentalCount(currentDate, stockList, dateCheck));             
                }
            values.add(calendarDto);
            }
        }
        return values;
    }



    public RentalCountDto rentalCount(LocalDate currentDate, List <Stock> stockList,  LocalDate dateCheck){
        RentalCountDto rentalCountDto = new RentalCountDto();
        int rentalCountValue = 0;
        LocalDateTime localDateTime =dateCheck.atStartOfDay();
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());        
        

        for(int s = 0; s < stockList.size(); s++){
            Stock stock = stockList.get(s);
            List <RentalManage> rentalManageList = rentalManageRepository.findByStockIdAndStatus(stock.getId(),RentalStatus.RENT_WAIT.getValue(), RentalStatus.RENTAlING.getValue());
            boolean allTrue = false;   
            if(rentalManageList.size() == 0){
                allTrue = true;
            }

            if(dateCheck.isBefore(currentDate)){                      //過去日
                rentalCountDto.setRentalCount("×");
                allTrue = false;
            }else{   
                for(int r = 0; r < rentalManageList.size(); r++){
                        int beforeDay;
                        int afterDay;   
                        RentalManage rentalManage = rentalManageList.get(r);
                        beforeDay = date.compareTo(rentalManage.getExpectedRentalOn());
                        afterDay = date.compareTo(rentalManage.getExpectedReturnOn());
                        if (beforeDay < 0 || afterDay > 0) {  
                            allTrue = true;   
                        }else{    
                            allTrue = false;                                 
                            break;
                        }
                }
            }
            
            if(allTrue){
                rentalCountValue = rentalCountValue + 1;
                rentalCountDto.setRentalCount(rentalCountValue);
                rentalCountDto.idList.add(stock.getId());                                   
            }else{
                rentalCountDto.setRentalCount("×");
            }
        }   
        rentalCountDto.setExpectedRentalOn(date);
        return rentalCountDto;  
        
    }

    @Transactional 
    public List<Stock> getList(List<String> idList){
        List <Stock> stockList = new ArrayList<>();

        for(int i = 0; i < idList.size(); i++){
            String stockId = idList.get(i);
            Optional<Stock> optionalStock = stockRepository.findById(stockId);
            Stock stock = optionalStock.orElse(new Stock());

            stockList.add(stock);  


        }
        return stockList;
    }
}
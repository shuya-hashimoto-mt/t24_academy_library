package jp.co.metateam.library.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.validation.FieldError;


import jakarta.validation.Valid;
import jp.co.metateam.library.constants.Constants;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.service.StockService;
import jp.co.metateam.library.values.RentalStatus;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;

/**
 * 貸出管理関連クラスß
 */
@Log4j2
@Controller
public class RentalManageController {

    private final AccountService accountService;
    private final RentalManageService rentalManageService;
    private final StockService stockService;

    @Autowired
    public RentalManageController(
        AccountService accountService, 
        RentalManageService rentalManageService, 
        StockService stockService
    ) {
        this.accountService = accountService;
        this.rentalManageService = rentalManageService;
        this.stockService = stockService;
    }

    /**
     * 貸出一覧画面初期表示
     * @param model
     * @return
     */
    @GetMapping("/rental/index")
    public String index(Model model) {
        // 貸出管理テーブルから全件取得
        List <RentalManage> rentalManageList = this.rentalManageService.findAll();

        // 貸出一覧画面に渡すデータをmodelに追加
        model.addAttribute("rentalManageList", rentalManageList);

        // 貸出一覧画面に遷移

        return "rental/index";
    }


    
    @GetMapping("/rental/add")
    public String add(Model model){
        List <Account> accounts = this.accountService.findAll();
        List <Stock> stockList = this.stockService.findAll();


        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        if(!model.containsAttribute("rentalManageDto")){
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }

        return "rental/add";
    }



    @PostMapping("/rental/add")
    public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra){
        
        try{
            if(result.hasErrors()){
                throw new Exception("Validdation error.");
            }
            //登録処理
            this.rentalManageService.save(rentalManageDto);

            return "redirect:/rental/index";
        }catch(Exception e){
            log.error(e.getMessage());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

            return "redirect:/rental/index";
        }
    }



    @GetMapping("/rental/{id}/edit")
    public String edit(@PathVariable("id") String id, Model model) {
        List <Account> accounts = this.accountService.findAll();
        List <Stock> stockList = this.stockService.findAll();

        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

        if(!model.containsAttribute("rentalManageDto")){
            model.addAttribute("rentalManageDto", new RentalManageDto());
        
        RentalManageDto rentalManageDto = new RentalManageDto();
        RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));
        
        rentalManageDto.setId(rentalManage.getId());     
        rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());
        rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
        rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
        rentalManageDto.setStockId(rentalManage.getStock().getId());
        rentalManageDto.setStatus(rentalManage.getStatus());

        model.addAttribute("rentalManageDto", rentalManageDto);    
        }  

        return "rental/edit";
    }



 @PostMapping("/rental/{id}/edit")
    public String update(@PathVariable("id") String id, @Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra, Model model) {
        try {
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }

            RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));
            Optional<String> validError = rentalManageDto.validStatus(rentalManage.getStatus());


            if(validError.isPresent()){
                result.addError(new FieldError("rentalManage","status",validError.get()));
                throw new Exception("Validation error.");
            }

            int rentalStatus = rentalManageDto.getStatus();
            if(rentalStatus == RentalStatus.RENT_WAIT.getValue() || rentalStatus == RentalStatus.RENTAlING.getValue()){
                //貸出ステータスが「貸出待ち」「貸出中」の貸出件数を取得する
                long rentalAvailableSattusCount = rentalManageService.countByStockIdAndStatusInAndIdNot(rentalManageDto.getStockId(), rentalManageDto.getId());
                //貸出可能な期間に該当する貸出件数を取得する
                long retanlAvailableTermCount = rentalManageService.countByStockIdAndStatusAndIdNotAndTermsIn(rentalManageDto.getStockId(), rentalManageDto.getId(),rentalManageDto.getExpectedReturnOn(),rentalManageDto.getExpectedRentalOn());

                if(rentalAvailableSattusCount != retanlAvailableTermCount){
                    ra.addFlashAttribute("erroMsg", "選択された在庫は現在、貸出できません");
                    throw new Exception("Stock unavaliable error.");
                }

                if(retanlAvailableTermCount < 1){
                    ra.addFlashAttribute("erroMsg", "選択された在庫は新規で貸出してください");
                    throw new Exception("Stock unavaliable error.");
                }
            }

            
            // 登録処理
            rentalManageService.update(Long.valueOf(id), rentalManageDto);  

            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

            List <Account> accounts = this.accountService.findAll();
            List <Stock> stockList = this.stockService.findAll();

            model.addAttribute("accounts", accounts);
            model.addAttribute("stockList", stockList);
            model.addAttribute("rentalStatus", RentalStatus.values());

            return "redirect:/rental/edit";
        }
    }
}

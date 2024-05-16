package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Optional;
import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.RentalManage;

@Repository
public interface RentalManageRepository extends JpaRepository<RentalManage, Long> {
    List<RentalManage> findAll();

	Optional<RentalManage> findById(Long id);

    @Query("SELECT COUNT(rm) FROM RentalManage rm WHERE rm.stock.id = ?1 AND rm.status IN (0, 1)")
    long countByStockIdAndStatusIn(String stock_id);
    @Query("SELECT COUNT(rm) FROM RentalManage rm WHERE rm.stock.id = ?1 AND rm.status IN (0, 1) AND rm.id <> ?2")
    long countByStockIdAndStatusInAndIdNot(String stock_id, Long id);
    @Query("SELECT COUNT(rm) FROM RentalManage rm WHERE rm.stock.id = ?1 AND rm.status IN (0, 1) AND (rm.expectedRentalOn > ?2 OR rm.expectedReturnOn < ?3)")
    long countByStockIdAndStatusAndTermsIn(String stock_id, Date expectedReturnOn, Date expectedRentalOn);
    @Query("SELECT COUNT(rm) FROM RentalManage rm WHERE rm.stock.id = ?1 AND rm.status IN (0, 1) AND rm.id <> ?2 AND (rm.expectedRentalOn > ?3 OR rm.expectedReturnOn < ?4)")
    long countByStockIdAndStatusAndIdNotAndTermsIn(String stock_id, Long id, Date expectedReturnOn, Date expectedRentalOn);

}
        
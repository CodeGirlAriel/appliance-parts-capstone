package com.wgu.capstone.repository;

import com.wgu.capstone.entity.PartSupplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PartSupplierRepository extends JpaRepository<PartSupplier, Long> {

    @Query("SELECT ps FROM PartSupplier ps LEFT JOIN FETCH ps.supplier WHERE ps.part.partId = :partId")
    List<PartSupplier> findByPart_PartId(String partId);
    
    @Query("SELECT ps FROM PartSupplier ps LEFT JOIN FETCH ps.part LEFT JOIN FETCH ps.supplier WHERE ps.partSupplierId = :partSupplierId")
    Optional<PartSupplier> findByIdWithRelations(Long partSupplierId);
}

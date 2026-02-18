package com.wgu.capstone.repository;

import com.wgu.capstone.entity.Part;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartRepository extends JpaRepository<Part, String> {

    List<Part> findByPartNameContainingIgnoreCase(String keyword);
    
    List<Part> findByPartIdContainingIgnoreCase(String keyword);
}

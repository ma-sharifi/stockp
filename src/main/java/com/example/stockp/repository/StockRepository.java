package com.example.stockp.repository;

import com.example.stockp.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for the Stock entity.
 */

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {}

package com.sdemo1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sdemo1.entity.FoodOption;

@Repository
public interface FoodOptionRepository extends JpaRepository<FoodOption, Integer> {
}

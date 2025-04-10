package com.sdemo1.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sdemo1.entity.RecipeStep;

@Repository
public interface RecipeStepRepository extends JpaRepository<RecipeStep, Integer> {
    Optional<RecipeStep> findByCookId(Integer cookId);
}

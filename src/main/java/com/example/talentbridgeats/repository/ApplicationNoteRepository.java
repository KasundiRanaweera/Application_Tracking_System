package com.example.talentbridgeats.repository;

import com.example.talentbridgeats.model.ApplicationNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationNoteRepository extends JpaRepository<ApplicationNote, Long> {
    List<ApplicationNote> findByApplicationId(Long applicationId);
}
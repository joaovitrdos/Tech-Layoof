package com.layoof.layoof.repository;

import com.layoof.layoof.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {

    List<VerificationCode> findAllByUserEmailAndUsedFalse(String email);
}

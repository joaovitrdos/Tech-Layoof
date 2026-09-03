package com.layoof.layoof.service;

import com.layoof.layoof.entity.User;
import com.layoof.layoof.repository.LayoofRepository;
import com.layoof.layoof.repository.UserRepository;
import com.layoof.layoof.util.ReputationScore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReputationService {

    private final LayoofRepository layoofRepository;
    private final UserRepository userRepository;

    @Transactional
    public void refresh(User author) {
        if (author == null) {
            return;
        }

        int score = ReputationScore.INITIAL;
        for (long balance : layoofRepository.reactBalancesByAuthor(author)) {
            score = ReputationScore.apply(score, balance);
        }

        author.setConfidenceScore(score);
        userRepository.save(author);
    }
}

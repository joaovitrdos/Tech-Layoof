package com.layoof.layoof.repository;

import com.layoof.layoof.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByAuthorUserIdOrderByCreatedAtDesc(UUID userId);
}

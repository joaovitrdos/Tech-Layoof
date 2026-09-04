package com.layoof.layoof.repository;

import com.layoof.layoof.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    boolean existsByEmail(String email);

    Optional<User> findByLinkedinURL(String linkedinURL);

    @Query(value = """
        select * 
        from tb_users u
        where 
            to_tsvector('portuguese', coalesce(u.name, ''))
            @@ plainto_tsquery('portuguese', :name)
        limit 20
        """, nativeQuery = true)
    List<User> searchByName(@Param("name") String name);


}

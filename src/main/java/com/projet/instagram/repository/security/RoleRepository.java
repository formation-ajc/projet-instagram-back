package com.projet.instagram.repository.security;

import java.util.Optional;

import com.projet.instagram.model.security.ERole;
import com.projet.instagram.model.security.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}

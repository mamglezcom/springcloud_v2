package com.mamglez.springcloud.msvc.users.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.mamglez.springcloud.msvc.users.entities.User;


public interface UserRepository extends CrudRepository<User, Long> {
    // Métodos adicionales personalizados si es necesario
    Optional<User> findByUsername(String username);
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.banburysymphony.orchestra.jpa;

import com.banburysymphony.orchestra.data.User;
import java.util.Optional;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Direct access to User objects
 * @author dave.settle@osinet.co.uk on 24 Aug 2022
 */
public interface UserRepository extends PagingAndSortingRepository<User, Integer> {

    public Optional<User> findByEmail(String email);
}

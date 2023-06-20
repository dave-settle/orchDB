/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.banburysymphony.orchestra.jpa;

import com.banburysymphony.orchestra.data.Role;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Mechanism for finding roles
 * @author dave.settle@osinet.co.uk on 21 Sept 2022
 */
public interface RoleRepository extends PagingAndSortingRepository<Role, Integer>, CrudRepository<Role, Integer> {

    public Optional<Role> findByAuthority(String name);
}

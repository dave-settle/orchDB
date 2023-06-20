/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.banburysymphony.orchestra.jpa;

import com.banburysymphony.orchestra.data.Venue;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Methods for accessing Venue records
 * @author dave.settle@osinet.co.uk on 19-Aug-2022
 */
public interface VenueRepository extends CrudRepository<Venue, Integer>, PagingAndSortingRepository<Venue, Integer> {
    
    public Optional<Venue> findByName(String name);

}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.banburysymphony.orchestra.jpa;

import org.springframework.data.repository.CrudRepository;
import com.banburysymphony.orchestra.data.Artist;
import java.util.Optional;
/**
 *
 * @author dave.settle@osinet.co.uk on 11-Aug-2022
 */
public interface ArtistRepository extends CrudRepository<Artist, Integer> {
    
    public Optional<Artist> findByName(String name);
    
}

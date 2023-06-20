/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.banburysymphony.orchestra.jpa;

import com.banburysymphony.orchestra.data.Engagement;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Interface for handling Engagement records
 * @author dave.settle@osinet.co.uk on 20 Aug 2022
 */
public interface EngagementRepository extends CrudRepository<Engagement, Integer>, PagingAndSortingRepository<Engagement, Integer> {

}

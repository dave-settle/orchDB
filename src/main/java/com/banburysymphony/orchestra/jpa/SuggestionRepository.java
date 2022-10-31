/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.banburysymphony.orchestra.jpa;

import com.banburysymphony.orchestra.data.Suggestion;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Repository interface for suggestions
 * @author dave.settle@osinet.co.uk on 31 Oct 2022
 */
public interface SuggestionRepository extends PagingAndSortingRepository<Suggestion, Integer>{

}

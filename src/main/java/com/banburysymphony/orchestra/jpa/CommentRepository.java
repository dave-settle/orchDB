/*
 */

package com.banburysymphony.orchestra.jpa;

/**
 * Repository for accessing comments
 * @author dave.settle@osinet.co.uk on 19 Oct 2022
 */
import com.banburysymphony.orchestra.data.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CommentRepository extends PagingAndSortingRepository<Comment, Integer> {
    
    @Query("select distinct c from Comment c join c.soloists s where s.artist.id = ?1")
    public List<Comment> findBySuggestion(Integer id);

}

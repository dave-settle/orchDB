/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.banburysymphony.orchestra.data;

/**
 *
 * @author dave.settle@osinet.co.uk on 21 Sept 2022
 */
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "roles")
public class Role implements GrantedAuthority, Serializable {
    
    public enum Name {
        USER("ROLE_USER", 1),
        TRUSTEE("ROLE_TRUSTEE", 2),
        CHAIR("ROLE_CHAIR", 3),
        TREASURER("ROLE_TREASURER", 4),
        SECRETARY("ROLE_SECRETARY", 5),
        ADMIN("ROLE_ADMIN", 6),
        PLANNER("ROLE_PLANNER", 7),
        CONDUCTOR("ROLE_CONDUCTOR", 8);
        
        private final String label;
        private int id;
        
        Name(String label, int id) {
            this.label = label;
            this.id = id;
        }
        @Override
        public String toString() {
            return label;
        }

        /**
         * @return the id
         */
        public int getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(int id) {
            this.id = id;
        }
        public String getLabel() {
            return label;
        }
    }
    
    private static final Logger log = LoggerFactory.getLogger(Role.class);
  
    protected Role() {}

    public Role(String auth) {
        this.authority = auth;
    }
    
    public Role(Name auth) {
        this.authority = auth.toString();
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(unique = true)
    private String authority;

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    public String toString() {
        return "Role(" + getAuthority() + ")";
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    /**
     * @param authority the authority to set
     */
    public void setAuthority(String authority) {
        this.authority = authority;
    }
    /**
     * Build a set of roles from a list of role IDs presented by the UI
     * @param required
     * @return 
     */
    public static Set<Role> findRoles(int[] required) {
        Set<Role> result = new HashSet<>();
        for(int id: required) {
            Role r = findRole(id);
            if(r != null)
                result.add(r);
        }
        return result;
    }
    /**
     * Convert a role ID into a Role object
     * @param id
     * @return 
     */
    public static Role findRole(int id) {
        for(Name n: Name.values()) {
            if(n.getId() == id) {
                return new Role(n);
            }
        }
        return null;
    }
    
    public static Role findRole(String name) {
        for(Name n: Name.values()) {
            if(n.getLabel().equals(name)) {
                return new Role(n);
            }
        }
        throw new IllegalArgumentException("no such role: " + name);
    }
}

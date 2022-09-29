/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.banburysymphony.orchestra.data;

/**
 *
 * @author dave.settle@osinet.co.uk on 24 Aug 2022
 */
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users")
public class User implements UserDetails {


    private static final Logger log = LoggerFactory.getLogger(User.class);
   
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id = -1;

    @Column(nullable = true, length = 200)
    private String firstname;
    
    @Column(nullable = true, length = 200)
    private String lastname;
    
    @NotBlank
    @Email(message = "Please enter a valid e-mail address")
    @Column(nullable = false, unique = true, length = 200)
    private String email;
     
    @Column(nullable = false, length = 500)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", 
            joinColumns = { @JoinColumn(name = "fk_user") }, 
            inverseJoinColumns = { @JoinColumn(name = "fk_role") })
    private Set<Role> roles = new HashSet<Role>();
    
    @Column
    private Boolean enabled = true;
    
    protected User() {}
    
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
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

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles();
    }
    
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isEnabled();
    }

    @Override
    public boolean isAccountNonLocked() {
        return isEnabled();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isEnabled();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean v) {
        enabled = v;
    }
    
    /**
     * @return the firstname
     */
    public String getFirstname() {
        return firstname;
    }

    /**
     * @param firstname the firstname to set
     */
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    /**
     * @return the lastname
     */
    public String getLastname() {
        return lastname;
    }

    /**
     * @param lastname the lastname to set
     */
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    /**
     * @return the roles
     */
    public Set<Role> getRoles() {
        return roles;
    }

    /**
     * @param roles the roles to set
     */
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    /**
     * Sometimes it's useful to know whether a user has a particular role
     * @param auth the role
     * @return true if this user has the given role
     */
    public boolean hasRole(GrantedAuthority auth) {
        return hasRole(auth.getAuthority());
    }
    public boolean hasRole(String name) {
        for(GrantedAuthority auth: getRoles()) {
            if(auth.getAuthority().equalsIgnoreCase(name))
                return true;
        }
        return false;
    }
}

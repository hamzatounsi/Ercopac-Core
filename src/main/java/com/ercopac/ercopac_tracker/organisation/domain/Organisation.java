package com.ercopac.ercopac_tracker.organisation.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "organisations")
public class Organisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private boolean active = true;

    public Organisation() {
    }

    public Organisation(String name, String code, boolean active) {
        this.name = name;
        this.code = code;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Object setStatus(String status) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setStatus'");
    }

    public Object setCountry(String country) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setCountry'");
    }

    public Object setDomain(String domain) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setDomain'");
    }
}
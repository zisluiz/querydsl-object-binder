package com.zisluiz.querydslbinder.test.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class State {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Column
    private String name;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "idcountry", foreignKey = @ForeignKey(name = "fk_state_country"), nullable = false)
    private Country country;
    
    @OneToMany(mappedBy = "state")
    private List<City> cities;
    
	public State(String name, Country country) {
		this.name = name;
		this.country = country;
	}
}

package com.zisluiz.querydslbinder.test.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Column
    private String name;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "idstate", foreignKey = @ForeignKey(name = "fk_city_state"), nullable = false)
    private State state;
    
	public City(String name, State state) {
		this.name = name;
		this.state = state;
	}
}

package com.sdemo1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;

@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name="food_item")
public class FoodItem {

    @Id
    @Column(name="f_is_id")
    private int id;

    @Column(name="food_id")
    private  int foodID;


    @Column(name="food_name")
    private String foodName;

    @Column(name="parent_id")
    private String parentID;

    @Column(name="food_img")
    private String foodImg;
    
    @Transient
    @JsonProperty("sID")
    private int sID;

    @Transient
    @JsonProperty("mID")
    private int mID;

    @Transient
    @JsonProperty("sName")
    private String sName;

    @Transient
    @JsonProperty("mName")
    private String mName;
}

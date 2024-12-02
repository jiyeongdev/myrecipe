package com.example;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class Employee {

    private Integer id;
    private String first;
    private String phone;
    private String startDate;

    public void setId(Integer id) {
        this.id = id;
    }

    @DynamoDbPartitionKey
    public Integer getId() {
        return this.id;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    @DynamoDbSortKey
    public String getStartDate() {
        return this.startDate;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getFirst() {
        return this.first;
    }
}
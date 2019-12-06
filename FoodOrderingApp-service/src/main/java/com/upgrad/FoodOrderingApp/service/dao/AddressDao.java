package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class AddressDao {

    @PersistenceContext
    private EntityManager entityManager;

    //Creates the customer address in the database
    public AddressEntity createAddress(AddressEntity addressEntity){
        this.entityManager.persist(addressEntity);
        return addressEntity;
    }

    //creates customer address record in the customer_address table
    public CustomerAddressEntity createCustomerAddress(CustomerAddressEntity customerAddressEntity){
        this.entityManager.persist(customerAddressEntity);
        return customerAddressEntity;
    }
}

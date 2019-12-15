package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class CustomerDao {

    @PersistenceContext
    private EntityManager entityManager;

    //Persisting the customer information of the created customer
    public CustomerEntity createCustomer(CustomerEntity customerEntity){
        this.entityManager.persist(customerEntity);
        return customerEntity;
    }

    //Returns a customer based on the give contact number
    public CustomerEntity getCustomerByContactNum(String contactNum){
        try{
            return (CustomerEntity)this.entityManager.createNamedQuery("customerByContactNum", CustomerEntity.class).setParameter("contactNum", contactNum).getSingleResult();
        }catch(NoResultException nre){
            return null;
        }
    }

    //Creates a record in customer_auth table
    public CustomerAuthEntity createAuthToken(CustomerAuthEntity customerAuthEntity){

        this.entityManager.persist(customerAuthEntity);
        return customerAuthEntity;
    }

    //Fetches the CustomerAuthEntity based on the access token
    public CustomerAuthEntity getCustomerAuthToken(final String accessToken){
        try{
            return entityManager.createNamedQuery("cusomerAuthTokenByAccessToken", CustomerAuthEntity.class).setParameter("accessToken", accessToken).getSingleResult();
        }catch(NoResultException nre){
            return null;
        }
    }

    //Updates the Customer information
    public void updateCustomer(CustomerEntity updatedCustomerEntity){
        this.entityManager.merge(updatedCustomerEntity);
    }

}

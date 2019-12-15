package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class StateDao {

    @PersistenceContext
    private EntityManager entityManager;

    //Get state entity by state UUID
    public StateEntity getStateByUuid(String StateUuid){
        try{
            return (StateEntity)this.entityManager.createNamedQuery("stateByStateUuid",StateEntity.class).setParameter("uuid", StateUuid).getSingleResult();
        }catch(NoResultException nre){
            return null;
        }
    }

    //List all states available in DB table
    public List<StateEntity> getAllStates(){
        try{
            return this.entityManager.createNamedQuery("allTheStates", StateEntity.class).getResultList();
        }catch(NoResultException nre){
            return null;
        }
    }

}

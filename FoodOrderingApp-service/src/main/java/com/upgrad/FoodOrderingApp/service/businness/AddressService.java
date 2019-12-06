package com.upgrad.FoodOrderingApp.service.businness;

import com.sun.jndi.cosnaming.IiopUrl;
import com.upgrad.FoodOrderingApp.service.dao.AddressDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerAddressDao;
import com.upgrad.FoodOrderingApp.service.dao.StateDao;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService {
    @Autowired
    private CustomerAddressDao customerAddressDao;

    @Autowired
    private AddressDao addressDao;

    @Autowired
    private StateDao stateDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity saveAddress(final AddressEntity addressEntity )throws SaveAddressException{
        //pincode must have digits only from 0 to 9 and must be of 6 digits
        String pinCodeRegex ="^[0-9]{6}$";
        if(addressEntity.getFlatBuilNumber().isEmpty()
                || addressEntity.getLocality().isEmpty()
                || addressEntity.getCity().isEmpty()
                || addressEntity.getPinCode().isEmpty()
                || addressEntity.getUuid().isEmpty()){
            throw new SaveAddressException("SAR-001", "No field can be empty.");
        }//pincode must be of the proper format
        else if (!addressEntity.getPinCode().matches(pinCodeRegex)){
            throw new SaveAddressException("SAR-002", "Invalid pincode");
        }//else create the address iin the database
        else {
            return addressDao.createAddress(addressEntity);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public StateEntity getStateByUuid(final String stateUuid) throws AddressNotFoundException, SaveAddressException{
        //if stateUUid is empty
        if (stateUuid.isEmpty()){
            throw new SaveAddressException("SAR-001", "No field can be empty.");
        }//if state is empty
        StateEntity stateEntity =stateDao.getStateByUuid(stateUuid);
        if(stateEntity == null){
            throw new AddressNotFoundException("ANF-002","No state by this id");
        }
        else {
            return stateEntity;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAddressEntity createCustomerAddress(final CustomerAddressEntity customerAddressEntity){
        return addressDao.createCustomerAddress(customerAddressEntity);
    }

}

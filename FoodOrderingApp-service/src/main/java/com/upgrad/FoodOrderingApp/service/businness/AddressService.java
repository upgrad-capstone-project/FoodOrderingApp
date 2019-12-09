package com.upgrad.FoodOrderingApp.service.businness;

// import com.sun.jndi.cosnaming.IiopUrl;
import com.upgrad.FoodOrderingApp.service.dao.AddressDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerAddressDao;
import com.upgrad.FoodOrderingApp.service.dao.StateDao;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional(propagation = Propagation.REQUIRED)
    public List<CustomerAddressEntity> getCustomerAddressesByCustomer (final CustomerEntity customerEntity){
        return customerAddressDao.getCustomerAddressListByCustomer(customerEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity getAddressByAddressUuid(final String addressUuid)throws AddressNotFoundException{
        //if address id is empty, throw exception
        if(addressUuid.isEmpty()){
            throw new AddressNotFoundException("ANF-005", "Address id can not be empty");
        }
        AddressEntity addressEntity = addressDao.getAddressByAddressUuid(addressUuid);
        //if address id is incorrect and no such address exist in database
        if(addressEntity == null){
            throw new AddressNotFoundException("ANF-003", "No address by this id");
        }
        else {
            return addressEntity;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAddressEntity getCustomerAddressByAddressId(final AddressEntity addressEntity){
        return customerAddressDao.getCustomerAddressByAddressId(addressEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public String deleteAddress(AddressEntity addressEntity, CustomerEntity signedInCustomer, CustomerEntity belongsToAddressEntity)throws AuthorizationFailedException{
        if(addressEntity.getActive() == 0){
            if(signedInCustomer.getUuid() != belongsToAddressEntity.getUuid()){
                throw new AuthorizationFailedException("ATHR-004", "You are not authorized to view/update/delete any one else's address ");
            }
            else{
                return addressDao.deleteAddress(addressEntity);
            }
        }
        else {
            addressEntity.setActive(0);
            return null; //need to check on this
        }

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<StateEntity> getAllStates(){
        return stateDao.getAllStates();
    }

}

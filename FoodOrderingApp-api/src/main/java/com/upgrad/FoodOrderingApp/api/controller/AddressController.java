package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.service.businness.AddressService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.api.model.SaveAddressResponse;
import com.upgrad.FoodOrderingApp.api.model.SaveAddressRequest;
import com.upgrad.FoodOrderingApp.api.model.AddressListResponse;
import com.upgrad.FoodOrderingApp.api.model.DeleteAddressResponse;
import com.upgrad.FoodOrderingApp.api.model.AddressList;
import com.upgrad.FoodOrderingApp.api.model.AddressListState;
import com.upgrad.FoodOrderingApp.api.model.StatesListResponse;
import com.upgrad.FoodOrderingApp.api.model.StatesList;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private CustomerService customerService;

    //Endpoint for creating/saving new addess for a customer
    //Creates an AddressEntity
    @RequestMapping(value = "/address", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveAddressResponse> saveAddress(SaveAddressRequest saveAddressRequest,
                                                           @RequestHeader("authorization") final String accessToken) throws AuthorizationFailedException, SaveAddressException, AddressNotFoundException {
        String[] bearerToken = accessToken.split("Bearer ");
        CustomerEntity customerEntity = null;
        if(bearerToken.length==1){
            throw new AuthorizationFailedException("ATHR-005","Use valid authorization format <Bearer accessToken>");
        } else {
            customerEntity = customerService.getCustomer(bearerToken[1]);
        }
        try{
            saveAddressRequest.getFlatBuildingName().isEmpty();
            saveAddressRequest.getLocality().isEmpty();
            saveAddressRequest.getCity().isEmpty();
            saveAddressRequest.getPincode().isEmpty();
            saveAddressRequest.getStateUuid().isEmpty();
        } catch(Exception e) {
            throw new SaveAddressException("SAR-001", "No field can be empty.");
        }

        String pinCode = addressService.validatePincode(saveAddressRequest.getPincode());
        StateEntity stateEntity = addressService.getStateByUUID(saveAddressRequest.getStateUuid());

        final AddressEntity addressEntity = new AddressEntity();
        addressEntity.setUuid(UUID.randomUUID().toString());
        addressEntity.setFlatBuilNo(saveAddressRequest.getFlatBuildingName());
        addressEntity.setLocality(saveAddressRequest.getLocality());
        System.out.println(saveAddressRequest.getCity());
        addressEntity.setCity(saveAddressRequest.getCity());
        addressEntity.setPincode(pinCode);
        addressEntity.setState(stateEntity);
        addressEntity.setActive(1);

        final AddressEntity persistedAddressEntity = addressService.saveAddress(addressEntity,customerEntity);

        SaveAddressResponse saveAddressResponse = new SaveAddressResponse()
                .id(persistedAddressEntity.getUuid()).status("ADDRESS SUCCESSFULLY REGISTERED");

        return new ResponseEntity<SaveAddressResponse>(saveAddressResponse, HttpStatus.CREATED);
    }

    //Endpoint to list all saved addresses of a customer
    @RequestMapping(value = "/address/customer", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AddressListResponse> getSavedAddresses(@RequestHeader("authorization") final String accessToken) throws AuthorizationFailedException {
        String[] bearerToken = accessToken.split("Bearer ");
        CustomerEntity customerEntity = null;
        if(bearerToken.length==1){
            throw new AuthorizationFailedException("ATHR-005","Use valid authorization format <Bearer accessToken>");
        } else {
            customerEntity = customerService.getCustomer(bearerToken[1]);
        }
        final List<AddressEntity> addressEntityList = addressService.getAllAddress(customerEntity);

        AddressListResponse addressListResponse = new AddressListResponse();
        Collections.reverse(addressEntityList);
        for(AddressEntity customerAddresses : addressEntityList ){
            AddressList addressList = new AddressList();
            addressList.setId(UUID.fromString(customerAddresses.getUuid()));
            addressList.setFlatBuildingName(customerAddresses.getFlatBuilNumber());
            addressList.setLocality(customerAddresses.getLocality());
            addressList.setCity(customerAddresses.getCity());
            addressList.setPincode(customerAddresses.getPinCode());

            StateEntity stateEntity = customerAddresses.getState();
            AddressListState addressListState = new AddressListState();
            addressListState.setId(UUID.fromString(stateEntity.getUuid()));
            addressListState.setStateName(stateEntity.getStateName());

            addressList.state(addressListState);

            addressListResponse.addAddressesItem(addressList);

        }
        return new ResponseEntity<AddressListResponse>(addressListResponse, HttpStatus.OK);
    }

    //Deleting an address of a customer
    // Only sets the active status to 0 for the address. Not completely removed from DB
    @RequestMapping(value = "/address/{address_id}", method = RequestMethod.DELETE)
    public ResponseEntity<DeleteAddressResponse> deleteAddress(@PathVariable("address_id") final String addressUuid,
                                                               @RequestHeader("authorization") final String accessToken) throws AuthorizationFailedException, AddressNotFoundException{
        if(addressUuid.isEmpty()){
            throw new AddressNotFoundException("ANF-005","Address id can not be empty");
        }
        String[] bearerToken = accessToken.split("Bearer ");
        CustomerEntity loggedInCustomer = null;
        if(bearerToken.length==1){
            throw new AuthorizationFailedException("ATHR-005","Use valid authorization format <Bearer accessToken>");
        } else {
            loggedInCustomer = customerService.getCustomer(bearerToken[1]);
        }

        System.out.println("loggedinCustomer: "+loggedInCustomer.getFirstName());
        final AddressEntity addressEntityToBeDeleted = addressService.getAddressByUUID(addressUuid,loggedInCustomer);
        final String uuid = addressService.deleteAddress(addressEntityToBeDeleted).getUuid();

        DeleteAddressResponse deleteAddressResponse = new DeleteAddressResponse()
                .id(UUID.fromString(uuid))
                .status("ADDRESS DELETED SUCCESSFULLY");

        return new ResponseEntity<DeleteAddressResponse>(deleteAddressResponse, HttpStatus.OK);

    }

    //List all States in the table
    //No API input
    @RequestMapping(value = "/states" , method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<StatesListResponse> getAllStates(){
        List<StateEntity> stateEntityList = new ArrayList<>();
        stateEntityList.addAll(addressService.getAllStates());

        StatesListResponse statesListResponse = new StatesListResponse();

        for( StateEntity stateEntity : stateEntityList){
            StatesList statesList = new StatesList();
            statesList.setId(UUID.fromString(stateEntity.getUuid()));
            statesList.setStateName(stateEntity.getStateName());
            statesListResponse.addStatesItem(statesList);
        }

        return new ResponseEntity<StatesListResponse>(statesListResponse, HttpStatus.OK);

    }
}

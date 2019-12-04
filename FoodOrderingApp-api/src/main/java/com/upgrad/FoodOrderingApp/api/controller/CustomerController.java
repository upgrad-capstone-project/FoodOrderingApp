package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.api.model.SignupCustomerResponse;
import com.upgrad.FoodOrderingApp.api.model.SignupCustomerRequest;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin
@Api(value = "CustomerEntity Controller", description = "Endpoints for Customer : Signup/Login/Logout/ChangePassword")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping(path = "/customer/signup",consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value="Registration for new customer")
    @RequestMapping(path = "/customer/signup", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE
            , produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> signup(@RequestBody (required = false) final SignupCustomerRequest signupCustomerRequest) throws SignUpRestrictedException{

        final CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setUuid(UUID.randomUUID().toString());
        customerEntity.setFirstName(signupCustomerRequest.getFirstName());
        customerEntity.setLastName(signupCustomerRequest.getLastName());
        customerEntity.setEmail(signupCustomerRequest.getEmailAddress());
        customerEntity.setContactNum(signupCustomerRequest.getContactNumber());
        customerEntity.setSalt("1234abc");
        customerEntity.setPassword(signupCustomerRequest.getPassword());

        final CustomerEntity newCustomerEntity = customerService.createNewCustomer(customerEntity);

        SignupCustomerResponse customerResponse = new SignupCustomerResponse().id(newCustomerEntity.getUuid()).status("CUSTOMER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SignupCustomerResponse>(customerResponse, HttpStatus.CREATED);

    }


}

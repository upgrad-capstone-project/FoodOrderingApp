package com.upgrad.FoodOrderingApp.service.businness;


import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class CustomerService {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;

    //Creates a new customer after performing checks on the fields
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity saveCustomer(final CustomerEntity customerEntity) throws SignUpRestrictedException {

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                            "[a-zA-Z0-9_+&*-]+)*@"+
                            "(?:[a-zA-Z0-9-]+\\.)+[a-z"+
                            "A-Z]{2,7}$";

        Pattern pattern = Pattern.compile(emailRegex);

        String contactNumRegex = "^[0-9]{10}$";

        //If the contact number already exists in the database
        if(customerDao.getCustomerByContactNum(customerEntity.getContactNum()) != null){
            throw new SignUpRestrictedException("SGR-001","This contact number is already registered! Try other contact number.");
        } //if any other field other than last name is null
        else if (customerEntity.getFirstName().isEmpty() ||
                   customerEntity.getContactNum().isEmpty() ||
                   customerEntity.getEmail().isEmpty() ||
                   customerEntity.getPassword().isEmpty()){
            throw new SignUpRestrictedException("SGR-005","Except last name all fields should be filled");
        } //If the email ID provided by the customer is not in the correct format
        else if (!pattern.matcher(customerEntity.getEmail()).matches()){
            throw new SignUpRestrictedException("SGR-002", "Invalid email-id format!");
        } //If the contact number provided by the customer is not in correct format
        else if (!customerEntity.getContactNum().matches(contactNumRegex)) {
            throw new SignUpRestrictedException("SGR-003", "Invalid contact number!");
        } //If the password provided by the customer is weak
        else if (weakPassword(customerEntity.getPassword())){
            throw new SignUpRestrictedException("SGR-004","Weak password!");
        } //Else, save the customer information in the database
        else {
            String password = customerEntity.getPassword();
            String[] encryptedText = this.passwordCryptographyProvider.encrypt(password);
            customerEntity.setSalt(encryptedText[0]);
            customerEntity.setPassword(encryptedText[1]);
            return this.customerDao.createCustomer(customerEntity);
        }
    }

    //Method to check password strength
    public boolean weakPassword (String password) {
        boolean weak = true;
        if(password.length()>=8){
          //  System.out.println("Length is fine");
            if(Pattern.matches(".*[0-9].*",password)){
            //    System.out.println("Contains digit");
                if(Pattern.matches(".*[A-Z].*",password)){
              //      System.out.println("Contains capital letter");
                    if(Pattern.matches(".*[#@$%&*!^].*",password)){
                //        System.out.println("Contains special character");
                        weak=false;
                    }
                }
            }
        }
        return weak;
    }

    //Authenticates a customer based on the contact number (as username) and password
    @Transactional(propagation= Propagation.REQUIRED)
    public CustomerAuthEntity authenticate (final String contactNum, final String password) throws AuthenticationFailedException{
       //Contact number should have only numbers from 0 to 9 and must be of 10 digits only
        if(contactNum.isEmpty() || password.isEmpty()){
            throw  new AuthenticationFailedException("ATH-003", "Incorrect format of decoded customer name and password");
        }
        CustomerEntity customerEntity = customerDao.getCustomerByContactNum(contactNum);
        if(customerEntity == null){
            throw new AuthenticationFailedException("ATH-001","This contact number has not been registered!");
        }

        final String encryptedPassword =passwordCryptographyProvider.encrypt(password, customerEntity.getSalt());
        //If the password is correct, save the customer login information in the database
        if(encryptedPassword.equals(customerEntity.getPassword())){
            JwtTokenProvider jwtToken = new JwtTokenProvider(encryptedPassword);
            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);
            final String authToken = jwtToken.generateToken(customerEntity.getUuid(), now, expiresAt);
            CustomerAuthEntity customerAuthToken = new CustomerAuthEntity();
            customerAuthToken.setCustomer(customerEntity);
            customerAuthToken.setLoginAt(now);
            customerAuthToken.setExpiresAt(expiresAt);
            customerAuthToken.setUuid(UUID.randomUUID().toString());
            customerAuthToken.setAccessToken(authToken);
            customerDao.createAuthToken(customerAuthToken);
            customerDao.updateCustomer(customerEntity);
            return customerAuthToken;
        } else {
            throw new AuthenticationFailedException("ATH-002","Invalid Credentials");
        }
    }

    //Logout endpoint function
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity logout(final String accessToken) throws AuthorizationFailedException{
        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthToken(accessToken);
        //if access token doesnt exist in database
        if(customerAuthEntity == null){
            throw new AuthorizationFailedException("ATHR-001","Customer is not Logged in.");
        }//if the access token exists in database but the customer has already logged out
        else if(customerAuthEntity != null && customerAuthEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        }//if the access token exists in database but the session has expired
        else if(customerAuthEntity != null && ZonedDateTime.now().isAfter(customerAuthEntity.getExpiresAt())){
            throw new AuthorizationFailedException("ATHR-003","Your session is expired. Log in again to access this endpoint.");
        }//else update the Logout At in the database and return the UUID of the logged out customer
        else{
            final ZonedDateTime now = ZonedDateTime.now();
            customerAuthEntity.setLogoutAt(now);
            return customerAuthEntity.getCustomer();
        }
    }

    //This method is the Bearer authorization method
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity getCustomer(final String accessToken)throws AuthorizationFailedException{

        CustomerAuthEntity customerAuthEntity=null;
        if(accessToken!=null){
            customerAuthEntity = customerDao.getCustomerAuthToken(accessToken);
        } else {
            throw new AuthorizationFailedException("ATHR-004","Access token cannot be null");
        }
        //if access token doesnt exist in databases
        if(customerAuthEntity == null){
            throw new AuthorizationFailedException("ATHR-001","Customer is not Logged in.");
        }//If access token exiats in database but the customer has already logged out
        else if (customerAuthEntity != null && customerAuthEntity.getLogoutAt()!= null){
            throw new AuthorizationFailedException("ATHR-002","Customer is logged out. Log in again to access this endpoint.");
        }//If access token exists in database but the session has expired
        else if (customerAuthEntity != null && ZonedDateTime.now().isAfter(customerAuthEntity.getExpiresAt())){
            throw new AuthorizationFailedException("ATHR-003","Your session is expired. Log in again to access this endpoint.");
        }
        else {
            return customerAuthEntity.getCustomer();
        }
    }

    //This method is used to update a customer's Firstname and/or Lastname
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updateCustomer(final CustomerEntity customerEntity)throws UpdateCustomerException{
        //If the customer's firstname is empty
//else update the customer information

        customerDao.updateCustomer(customerEntity);
        return customerEntity;
    }

    //This method is used to update customer's password as mentioned in the new password field
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updateCustomerPassword(final String oldPswrd, final String newPswrd, final CustomerEntity customerEntity) throws UpdateCustomerException{
        //Check if old password or new password field is empty or not
        try {
            oldPswrd.isEmpty();
            newPswrd.isEmpty();
        } catch (Exception e){
            throw new UpdateCustomerException("UCR-003","No field should be empty");
        }
       if (!passwordCryptographyProvider.encrypt(oldPswrd,customerEntity.getSalt()).equals(customerEntity.getPassword())) {
           throw new UpdateCustomerException("UCR-004", "Incorrect old password!");
        } else if (weakPassword(newPswrd)) {

           throw new UpdateCustomerException("UCR-001", "Weak password!");
        }
        else {
            String[] encryptPswrd = this.passwordCryptographyProvider.encrypt(newPswrd);
            customerEntity.setSalt(encryptPswrd[0]);
            customerEntity.setPassword(encryptPswrd[1]);
           customerDao.updateCustomer(customerEntity);
            return customerEntity;
        }
    }

}

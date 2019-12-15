package com.upgrad.FoodOrderingApp.service.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/*Defining the Customer Entity based on Customer table*/
@Entity
@Table(
        name = "customer"
)

/*Fetch customer based on their contact number*/
@NamedQueries(
        @NamedQuery(name= "customerByContactNum", query= "select c from CustomerEntity c where c.contactNum = :contactNum")
)

public class CustomerEntity implements Serializable {

    @Id
    @Column(/*The primary key of the customer table*/
            name = "ID"
    )
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private long id;

    @Column(/*Must be unique and not null*/
            name = "UUID"
    )
    @NotNull
    @Size(
            max = 200
    )
    private String uuid;

    @Column(/*First name cannot be null*/
            name = "FIRSTNAME"
    )
//    @NotNull
    @Size(
            max = 30
    )
    private String firstName;

    @Column(/*Last name can be null*/
            name = "LASTNAME"
    )
    @Size(
            max = 30
    )
    private String lastName;

    @Column(/*Email can be null*/
            name = "EMAIL"
    )
    @Size(
            max = 50
    )
    private String email;

    @Column(/*Contact number must be unique and not null*/
            name = "CONTACT_NUMBER"
    )
    @Size(
            max = 30
    )
    private String contactNum;

    @Column(
            name = "PASSWORD"
    )
//    @NotNull
    private String password;

    @Column(
            name = "SALT"
    )
    @NotNull
    @Size(
            max = 200
    )
    private String salt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNum() {
        return contactNum;
    }

    public void setContactNum(String contactNum) {
        this.contactNum = contactNum;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Override
    public boolean equals(Object obj) {
        return new EqualsBuilder().append(this, obj).isEquals();
    }


    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this).hashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}

package com.csye6220.huskyamazon.service;

import com.csye6220.huskyamazon.entity.Address;
import com.csye6220.huskyamazon.entity.User;
import java.util.List;

public interface AddressService {
    void addAddress(User user, Address address);
    void deleteAddress(Long addressId, User user);
    List<Address> getAddressesByUser(User user);
}
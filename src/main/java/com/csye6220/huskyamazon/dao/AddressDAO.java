package com.csye6220.huskyamazon.dao;

import com.csye6220.huskyamazon.entity.Address;
import com.csye6220.huskyamazon.entity.User;
import java.util.List;

public interface AddressDAO {
    void save(Address address);
    void delete(Address address);
    Address findById(Long id);
    List<Address> findByUser(User user);
}
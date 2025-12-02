package com.csye6220.huskyamazon.service.impl;

import com.csye6220.huskyamazon.dao.AddressDAO;
import com.csye6220.huskyamazon.dao.UserDAO;
import com.csye6220.huskyamazon.entity.Address;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressDAO addressDAO;
    private final UserDAO userDAO;

    @Autowired
    public AddressServiceImpl(AddressDAO addressDAO, UserDAO userDAO) {
        this.addressDAO = addressDAO;
        this.userDAO = userDAO;
    }

    @Override
    @Transactional
    public void addAddress(User user, Address address) {
        // 确保关联到的是持久化状态的 User
        User managedUser = userDAO.findById(user.getId());
        address.setUser(managedUser);
        addressDAO.save(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long addressId, User user) {
        Address address = addressDAO.findById(addressId);
        // 安全检查：确保只能删除自己的地址
        if (address != null && address.getUser().getId().equals(user.getId())) {
            addressDAO.delete(address);
        } else {
            throw new RuntimeException("Address not found or unauthorized");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Address> getAddressesByUser(User user) {
        return addressDAO.findByUser(user);
    }
}
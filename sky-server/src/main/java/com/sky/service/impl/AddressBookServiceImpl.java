package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.context.UserContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {
    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 根据id查询地址
     */
    @Override
    public AddressBook getById(Long id) {

        return addressBookMapper.selectById(id);
    }

    /**
     * 根据用户id查询地址列表
     */
    @Override
    public List<AddressBook> list(Long userId) {
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, userId);
        return addressBookMapper.selectList(queryWrapper);
    }

    /**
     * 根据用户id查询默认地址
     */
    @Override
    public AddressBook getDefaultAddressBook(Long currentId) {

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, currentId)
                    .eq(AddressBook::getIsDefault, 1);
        AddressBook addressBook = addressBookMapper.selectOne(queryWrapper);
        return addressBook;
    }

/**
 * 添加地址
 */
    @Override
    public void addAddressBook(AddressBook addressBook) {
        addressBook.setUserId(UserContext.getCurrentId());
        // 首次添加地址时设为默认地址
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, addressBook.getUserId());
        if (addressBookMapper.selectCount(queryWrapper) == 0) {
            addressBook.setIsDefault(1);
        }
        addressBookMapper.insert(addressBook);
    }

    /**
     * 修改地址
     */
    @Override
    public void updateAddressBook(AddressBook addressBook) {
        addressBookMapper.updateById(addressBook);
    }

    /**
     * 删除地址
     */
    @Override
    public void deleteAddressBook(Long id) {
        addressBookMapper.deleteById(id);
    }

    @Override
    public void setDefaultAddressBook(Long id) {

        // 清理当前用户之前设置的默认地址,把所有地址的默认标志设为0
        AddressBook addressBook = new AddressBook();
        addressBook.setIsDefault(0);
        Long currentId = UserContext.getCurrentId();
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, currentId);
        addressBookMapper.update(addressBook, queryWrapper);

        // 设置当前地址为默认地址
        addressBook.setId(id);
        addressBook.setIsDefault(1);
        addressBookMapper.updateById(addressBook);
    }
}

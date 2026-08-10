package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

public interface AddressBookService {
    /**
     * 根据id查询地址
     */
    AddressBook getById(Long id);

    /**
     * 根据用户id查询默认地址
     */
    AddressBook getDefaultAddressBook(Long currentId);

    /**
     * 添加地址
     */
    void addAddressBook(AddressBook addressBook);

    /**
     * 修改地址
     */
    void updateAddressBook(AddressBook addressBook);

    /**
     * 删除地址
     */
    void deleteAddressBook(Long id);

    /**
     * 设置默认地址
     */
    void setDefaultAddressBook(Long id);

    /**
     * 查询地址列表
     */
    List<AddressBook> list(Long userId);
}

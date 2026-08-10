package com.sky.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.context.UserContext;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
public class addressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 查询当前用户的地址列表
     */
    @GetMapping("/list")
    public Result<List<AddressBook>> list() {
        Long userId = UserContext.getCurrentId();
        List<AddressBook> list = addressBookService.list(userId);
        return Result.success(list);
    }

    /**
     * 根据用户id查询默认地址
     */
    @GetMapping("/default")
    public Result<AddressBook> getDefaultAddressBook() {
        Long id = UserContext.getCurrentId();
        System.out.println(id);
        AddressBook addressBook = addressBookService.getDefaultAddressBook(id);
        return Result.success(addressBook);
    }

    /**
     * 添加地址
     */
    @PostMapping
    public Result<String> addAddressBook(@RequestBody AddressBook addressBook) {
        addressBookService.addAddressBook(addressBook);
        return Result.success("添加地址成功");
    }

    /**
     * 修改地址
     */
    @PutMapping
    public Result<String> updateAddressBook(@RequestBody AddressBook addressBook) {
        addressBookService.updateAddressBook(addressBook);
        return Result.success("修改地址成功");
    }

    /**
     * 删除地址
     */
    @DeleteMapping
    public Result<String> deleteAddressBook(@RequestParam Long id) {
        addressBookService.deleteAddressBook(id);
        return Result.success("删除地址成功");
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/default")
    public Result<String> setDefaultAddressBook(@RequestBody(required = false) AddressBook addressBook) {
        Long id = addressBook.getId();
        addressBookService.setDefaultAddressBook(id);
        return Result.success("设置默认地址成功");
    }
}

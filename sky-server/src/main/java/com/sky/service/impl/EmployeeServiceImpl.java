package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger log = LogManager.getLogger(EmployeeServiceImpl.class);
    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = DigestUtils.md5DigestAsHex(employeeLoginDTO.getPassword().getBytes());

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //使用DigestUtils进行MD5加密
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }


    /**
     * 新增员工
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

        BeanUtils.copyProperties(employeeDTO, employee);

        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        employee.setStatus(StatusConstant.ENABLE);

        employeeMapper.insert(employee);
    }

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @Override
    public PageResult page(Integer page, Integer pageSize, String name) {
        Page<Employee> pageParam = new Page<>(page, pageSize);

        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            queryWrapper.like(Employee::getName, name);
        }
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        employeeMapper.selectPage(pageParam, queryWrapper);

        return new PageResult(pageParam.getTotal(), pageParam.getRecords());
    }

    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        if (status == StatusConstant.ENABLE)
            employeeMapper.update(Employee.builder().status(status).build(), new LambdaQueryWrapper<Employee>().eq(Employee::getId, id));
        else
            employeeMapper.update(Employee.builder().status(status).build(), new LambdaQueryWrapper<Employee>().eq(Employee::getId, id));
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.selectById(id);
        return employee;
    }

    /**
     * 编辑员工信息
     * @param employeeDTO
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.updateById(employee);
    }


    /**
     * 修改密码
     * @param passwordEditDTO
     */
    @Override
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        Long id = BaseContext.getCurrentId();
        Employee employee = employeeMapper.selectById(id);
        String oldPassword = employee.getPassword();

        if(DigestUtils.md5DigestAsHex(passwordEditDTO.getOldPassword().getBytes()).equals(oldPassword)){
            employeeMapper.update(Employee.builder().password(DigestUtils.md5DigestAsHex(passwordEditDTO.getNewPassword().getBytes())).build(), new LambdaQueryWrapper<Employee>().eq(Employee::getId, id));
        }else {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
    }


}

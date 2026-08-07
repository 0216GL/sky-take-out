package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.annotation.AutoFill;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    @Override
    @AutoFill(OperationType.INSERT)
    int insert(Employee employee);

    @Override
    @AutoFill(OperationType.UPDATE)
    int updateById(@Param("et") Employee employee);
}

package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.annotation.AutoFill;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SetmealMapper extends BaseMapper<Setmeal> {

    @Override
    @AutoFill(OperationType.INSERT)
    int insert(Setmeal setmeal);

    @Override
    @AutoFill(OperationType.UPDATE)
    int updateById(@Param("et") Setmeal setmeal);

    /**
     * 按状态统计套餐数量（1 起售中，0 已停售，见 StatusConstant）
     */
    @Select("select count(id) from setmeal where status = #{status}")
    Integer countByStatus(Integer status);
}

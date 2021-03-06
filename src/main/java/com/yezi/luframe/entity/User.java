package com.yezi.luframe.entity;

import com.yezi.luframe.base.BaseEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 测试用户表
 *
 * @author yezi
 * @date 2019/3/6 10:07
 */
@Data
@Entity
@Table(name = "user")
public class User extends BaseEntity {

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 地址
     */
    private String address;

    /**
     * 密码
     */
    private String password;

    /**
     * 年龄
     */
    private Integer age;


}

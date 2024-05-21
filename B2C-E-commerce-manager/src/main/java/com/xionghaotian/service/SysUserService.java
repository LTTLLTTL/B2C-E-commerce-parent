package com.xionghaotian.service;

import com.xionghaotian.dto.system.LoginDto;
import com.xionghaotian.vo.system.LoginVo;

/**
 * @ClassName SysUserService
 * @Description SysUserService接口
 * @Author XiongHaoTian
 * @Date 2024年05月19日 21:19
 * @Version 1.0
 */
public interface SysUserService {
    /**
     * 根据用户名查询用户数据
     * @return
     */
    public abstract LoginVo login(LoginDto loginDto) ;
}

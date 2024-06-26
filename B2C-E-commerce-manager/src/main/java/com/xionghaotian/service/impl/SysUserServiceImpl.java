package com.xionghaotian.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.xionghaotian.dto.system.LoginDto;
import com.xionghaotian.entity.system.SysUser;
import com.xionghaotian.exception.GuiguException;
import com.xionghaotian.mapper.SysUserMapper;
import com.xionghaotian.service.SysUserService;
import com.xionghaotian.vo.common.ResultCodeEnum;
import com.xionghaotian.vo.system.LoginVo;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName SysUserServiceImpl
 * @Description SysUserService实现类
 * @Author XiongHaoTian
 * @Date 2024年05月19日 21:20
 * @Version 1.0
 */
@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper ;

    @Resource
    private RedisTemplate<String , String> redisTemplate ;

    /**
     * 用户登录
     * @param loginDto 登录传输对象，包含用户名、密码、验证码和验证码键名
     * @return LoginVo 登录响应对象，包含令牌和刷新令牌
     */
    @Override
    public LoginVo login(LoginDto loginDto) {

        //校检验证码是否正确
        String captcha = loginDto.getCaptcha();     // 用户输入的验证码
        String codeKey = loginDto.getCodeKey();     // redis中验证码的数据key

        // 从Redis中获取验证码
        String redisCode = redisTemplate.opsForValue().get("user:login:validatecode:" + codeKey);
        if(StrUtil.isEmpty(redisCode) || !StrUtil.equalsIgnoreCase(redisCode , captcha)) {
            //验证码错误
            throw new GuiguException(ResultCodeEnum.VALIDATECODE_ERROR) ;
        }

        // 验证通过删除redis中的验证码
        redisTemplate.delete("user:login:validatecode:" + codeKey) ;


        // 根据用户名查询用户
        SysUser sysUser = sysUserMapper.selectByUserName(loginDto.getUserName());
        if(sysUser == null) {
//            throw new RuntimeException("用户名或者密码错误") ;
            throw new GuiguException(ResultCodeEnum.LOGIN_ERROR);
        }

        // 验证密码是否正确
        String inputPassword = loginDto.getPassword();
        String md5InputPassword = DigestUtils.md5DigestAsHex(inputPassword.getBytes());
        if(!md5InputPassword.equals(sysUser.getPassword())) {
//            throw new RuntimeException("用户名或者密码错误") ;
            throw new GuiguException(ResultCodeEnum.LOGIN_ERROR);
        }

        // 生成令牌，保存数据到Redis中
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set("user:login:" + token , JSON.toJSONString(sysUser) , 30 , TimeUnit.MINUTES);

        // 构建响应结果对象
        LoginVo loginVo = new LoginVo() ;
        loginVo.setToken(token);
        loginVo.setRefresh_token("");

        // 返回
        return loginVo;
    }

    /**
     * 根据token获取用户信息。
     *
     * @param token 用户登录时获取的令牌，用于识别用户身份。
     * @return SysUser 返回用户系统信息的对象。
     */
    @Override
    public SysUser getUserInfo(String token) {
        // 从Redis中根据token获取用户信息的JSON字符串
        String userJson = redisTemplate.opsForValue().get("user:login:" + token);
        // 将JSON字符串解析为SysUser对象
        return JSON.parseObject(userJson , SysUser.class) ;
    }

    /**
     * 用户登出功能。
     * 通过给定的token从Redis中删除对应的登录记录，实现用户登出。
     *
     * @param token 用户的登录令牌，用于唯一标识一个登录会话。
     */
    @Override
    public void logout(String token) {
        // 从Redis中删除指定token的登录记录
        redisTemplate.delete("user:login:" + token) ;
    }


}
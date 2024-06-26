package com.xionghaotian.controller;

import com.xionghaotian.AuthContextUtil;
import com.xionghaotian.dto.system.LoginDto;
import com.xionghaotian.entity.system.SysUser;
import com.xionghaotian.service.SysUserService;
import com.xionghaotian.service.ValidateCodeService;
import com.xionghaotian.vo.common.Result;
import com.xionghaotian.vo.common.ResultCodeEnum;
import com.xionghaotian.vo.system.LoginVo;
import com.xionghaotian.vo.system.ValidateCodeVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName IndexController
 * @Description 登录接口逻辑控制层
 * @Author XiongHaoTian
 * @Date 2024年05月19日 19:17
 * @Version 1.0
 */

@Tag(name = "用户接口")
@RestController
@RequestMapping(value = "/admin/system/index")
public class IndexController {

    @Autowired
    private SysUserService sysUserService ;

    @Autowired
    private ValidateCodeService validateCodeService ;

    //生成图片验证码
    @Operation(summary = "生成图片验证码")
    @GetMapping(value = "/generateValidateCode")
    public Result<ValidateCodeVo> generateValidateCode() {
        ValidateCodeVo validateCodeVo = validateCodeService.generateValidateCode();
        return Result.build(validateCodeVo , ResultCodeEnum.SUCCESS) ;
    }

    @Operation(summary = "登录接口")
    @PostMapping(value = "/login")
    public Result<LoginVo> login(@RequestBody LoginDto loginDto) {
        LoginVo loginVo = sysUserService.login(loginDto) ;
        return Result.build(loginVo , ResultCodeEnum.SUCCESS) ;
    }

    //获取用户信息接口
    //1.从请求头获取token
    //2.根据token去查询redis获取用户信息
    //3.用户信息返回
//    @Operation(summary = "获取用户信息接口")
//    @GetMapping(value = "/getUserInfo")
//    public Result<SysUser> getUserInfo(@RequestHeader(name = "token") String token) {
//        SysUser sysUser = sysUserService.getUserInfo(token) ;
//        return Result.build(sysUser , ResultCodeEnum.SUCCESS) ;
//    }

    //获取用户信息接口优化版本
    //通过ThreadLocal获取用户信息
    @Operation(summary = "获取用户信息接口")
    @GetMapping(value = "/getUserInfo")
    public Result<SysUser> getUserInfo() {
        return Result.build(AuthContextUtil.get(), ResultCodeEnum.SUCCESS);
    }

    //用户退出功能接口
    @Operation(summary = "用户退出功能接口")
    @GetMapping(value = "/logout")
    public Result logout(@RequestHeader(value = "token") String token) {
        sysUserService.logout(token) ;
        return Result.build(null , ResultCodeEnum.SUCCESS) ;
    }

}

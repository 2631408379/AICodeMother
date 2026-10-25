package com.dongyu.yuaicodemother.aop;

import com.dongyu.yuaicodemother.annotation.AuthCheck;
import com.dongyu.yuaicodemother.exception.BusinessException;
import com.dongyu.yuaicodemother.exception.ErrorCode;
import com.dongyu.yuaicodemother.model.entity.User;
import com.dongyu.yuaicodemother.model.enums.UserRoleEnum;
import com.dongyu.yuaicodemother.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

//切面类，用于拦截请求作权限校验
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    //切入点。说明要拦截哪些方法
    @Around("@annotation(authCheck)")
    public Object doIntercept(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        //获取方法所需权限
        String mustRole = authCheck.mustRole();
        //获取request请求对象
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //获取用户登录信息
        User loginUser = userService.getLoginUser(request);
        //将方法所需权限转成枚举类，便于比较
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        //若所需权限为空，直接放行
        if(mustRoleEnum == null){
            return joinPoint.proceed();
        }
        //校验用户是否具有所需权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());

        if(userRoleEnum == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        //判断是否具有管理员权限
        if(UserRoleEnum.ADMIN.equals(mustRoleEnum)&&!UserRoleEnum.ADMIN.equals(userRoleEnum)){
            //该用户不是管理员，抛出异常
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        //放行
        return joinPoint.proceed();
    }
}

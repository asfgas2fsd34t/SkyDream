package com.dream.shiro;

import cn.hutool.core.bean.BeanUtil;
import com.dream.pojo.AccountProfile;
import com.dream.pojo.MUser;
import com.dream.service.MUserService;
import com.dream.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


public class MyRealm extends AuthorizingRealm {

    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    MUserService userService;
    @Override
    public boolean supports(AuthenticationToken token) {

        return token instanceof JwtToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        System.out.println("获取权限");
        AccountProfile principal = (AccountProfile) principalCollection.getPrimaryPrincipal();

        MUser byId = userService.getById(principal.getId());
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addRole(byId.getRole());
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {

        JwtToken jwtToken= (JwtToken) authenticationToken;

        String subject = jwtUtils.getClaimByToken((String) jwtToken.getPrincipal()).getSubject();

        MUser user = userService.getById(subject);
        if(user == null){
            throw new UnknownAccountException("账户不存在");
        }
        if(user.getStatus() == -1){
            throw new LockedAccountException("账户被锁定");
        }
        System.out.println("认证");
        AccountProfile profile = new AccountProfile();
        BeanUtil.copyProperties(user,profile);//将user数据转移到profile
        return new SimpleAuthenticationInfo(profile,jwtToken.getCredentials(),getName());
    }
}

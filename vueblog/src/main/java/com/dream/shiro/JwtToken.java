package com.dream.shiro;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.HostAuthenticationToken;
import org.apache.shiro.authc.RememberMeAuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;

import javax.jws.soap.SOAPBinding;

@Data
@NoArgsConstructor
public class JwtToken implements HostAuthenticationToken, RememberMeAuthenticationToken {
    private String host;
    private String token;
    private boolean rememberMe;
    public JwtToken(String s){
        this.token=s;
        this.rememberMe=false;
        this.host=(String)null;
    }
    @Override
    public String getHost() {
        return host;
    }

    @Override
    public boolean isRememberMe() {
        return false;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }




}

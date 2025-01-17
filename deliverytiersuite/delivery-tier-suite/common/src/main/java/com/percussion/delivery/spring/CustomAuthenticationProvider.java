/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.delivery.spring;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomAuthenticationProvider  implements
        AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

@Override
public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
    PreAuthenticatedAuthenticationToken sessionUserDetails =
        (PreAuthenticatedAuthenticationToken) token.getDetails();
        List<GrantedAuthority> authorities = (List<GrantedAuthority>) sessionUserDetails.getAuthorities();
        return new User(sessionUserDetails.getName(),(String)sessionUserDetails.getCredentials(), true, true, true, true, authorities);
        }


}

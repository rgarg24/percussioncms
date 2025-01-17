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
package com.percussion.user.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.data.PSAbstractNamedObject;
import net.sf.oval.configuration.annotation.IsInvariant;
import net.sf.oval.constraint.Length;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;
import net.sf.oval.constraint.ValidateWithMethod;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * A user in the system and their associated roles.
 * 
 * @author DavidBenua
 * @author adamgent
 */
@XmlRootElement(name = "User")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonRootName("User")
public class PSUser extends PSAbstractNamedObject
{
    private static final long serialVersionUID = 1L;
    /**
     * Message used when user name contains invalid characters.
     */
    private static final String INVALID_CHAR_ERROR_MSG =
        "The username should be 4-20 characters long. OR " +
        "The user name contains an invalid character. " +
        "The valid characters of a user name are 'a' to 'z', 'A' to 'Z' and '0' to '9'.";
    
    /**
     * Message used when user name is too long.
     */
    private static final String NAME_LENGTH_ERROR_MSG = 
        "The maximum length of a user name is 50 characters.";
    
    private String password; 
    
    private String email = "";
    
    private PSUserProviderType providerType = PSUserProviderType.INTERNAL;

    public boolean isCreateUser() {
        return isCreateUser;
    }

    public void setCreateUser(boolean createUser) {
        isCreateUser = createUser;
    }

    private boolean isCreateUser;
    
    /**
     * A user has to be in at least one roll.
     */
    @NotEmpty
    @NotNull
    private List<String> roles;
    
    public PSUser()
    {
        roles = new ArrayList<>();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @IsInvariant
    @NotNull
    @NotBlank
    @Length(max = 50, message = NAME_LENGTH_ERROR_MSG)
    @ValidateWithMethod(methodName = "isValidName", parameterType = String.class, 
            message=INVALID_CHAR_ERROR_MSG)
    public String getName()
    {
        return super.getName();
    }
        
    @Override
    protected boolean isValidName(String name) {
        if (getProviderType() != PSUserProviderType.INTERNAL)
            return true;
        if(isCreateUser()){
            return name.matches("^[a-zA-Z0-9]([._-](?![._-])|[a-zA-Z0-9]){2,18}[a-zA-Z0-9]$") && super.isValidName(name);
        }else{
            return super.isValidName(name);
        }

    }
    
    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }
    
    /**
     * @param password the password to set
     */
    public void setPassword(String password)
    {
        this.password = password;
    }
        
    /**
     * @return never <code>null</code> might be empty.
     */
    @NotNull
    public String getEmail()
    {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email)
    {
        this.email = email;
    }

    /**
     * @return the roles
     */
    public List<String> getRoles()
    {
        return roles;
    }
    
    /**
     * @param roles the roles to set
     */
    public void setRoles(List<String> roles)
    {
        this.roles = roles;
    }
    
    /**
     * Where the authentication is done for this user.
     * <p>
     * If not set the default {@link PSUserProviderType#INTERNAL} 
     * will be returned.
     * 
     * @return never <code>null</code>.
     */
    public PSUserProviderType getProviderType()
    {
        return providerType;
    }

    public void setProviderType(PSUserProviderType providerType)
    {
        this.providerType = providerType;
    }

    @Override
    public PSUser clone() throws CloneNotSupportedException {
        PSUser user = (PSUser) super.clone();
        if (this.getRoles() != null) {
            user.setRoles(new ArrayList<>(this.getRoles()));
        }
        return user;
    }  
    
}

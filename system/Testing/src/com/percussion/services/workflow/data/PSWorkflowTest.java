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
package com.percussion.services.workflow.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link PSWorkflow} class.
 */
public class PSWorkflowTest
{
   public PSWorkflowTest()
   {
   }
   
   private PSWorkflowRole createWorkflowRole(int roleId)
   {
      PSWorkflowRole role = new PSWorkflowRole();
      role.setGUID(new PSGuid(PSTypeEnum.WORKFLOW_ROLE, roleId));
      role.setName("role_" + roleId);
      role.setDescription("desc_" + roleId);
      
      return role;
   }

   @Test
   @Ignore
   //TODO:  Fix me.  The toXML on state is missing one of the aging transitions for some reason.
   public void testAll() throws Exception
   {
      PSWorkflowRole role = createWorkflowRole(24);
      PSWorkflowRole role2 = new PSWorkflowRole();
      role2.fromXML(role.toXML());
      assertEquals(role, role2);
      
      List<PSWorkflowRole> roles = new ArrayList<PSWorkflowRole>();
      roles.add(role);
      role2 = createWorkflowRole(25);
      roles.add(role2);
      Set<Integer> roleIds = new HashSet<Integer>();
      roleIds.add(role.getGUID().getUUID());
      roleIds.add(role2.getGUID().getUUID());
      Set<String> roleNames = new HashSet<String>();
      roleNames.add(role.getName());
      roleNames.add(role2.getName());
      
      List<String> recipients = new ArrayList<String>();
      recipients.add("recipient_1");
      recipients.add("recipient_2");
      
      PSNotification notification = new PSNotification();
      notification.setGUID(new PSGuid(PSTypeEnum.WORKFLOW_NOTIFICATION, 48));
      notification.setRecipients(recipients);
      notification.setCCRecipients(recipients);
      PSNotification notification2 = new PSNotification();
      notification2.fromXML(notification.toXML());
      assertEquals(notification, notification2);
      
      List<PSNotification> notifications = new ArrayList<PSNotification>();
      notifications.add(notification);
      notifications.add(notification2);
      
      PSTransition transition = createTransition(72, roles, notifications);
      PSTransition transition2 = new PSTransition();
      transition2.fromXML(transition.toXML());
      assertEquals(transition, transition2);
      
      List<PSTransition> transitions = new ArrayList<PSTransition>();
      transitions.add(transition);
      transition2 = createTransition(82, roles, notifications);
      transitions.add(transition2);
      
      PSAgingTransition agingTransition = createAgingTransition(60);
      PSAgingTransition agingTransition2 = new PSAgingTransition();
      agingTransition2.fromXML(agingTransition.toXML());
      assertEquals(agingTransition, agingTransition2);
      
      List<PSAgingTransition> agingTransitions = new ArrayList<PSAgingTransition>();
      agingTransitions.add(agingTransition);
      agingTransition2 = createAgingTransition(50);
      agingTransitions.add(agingTransition2);
      
      PSState state = new PSState();
      state.setName("state_1");
      state.setAgingTransitions(agingTransitions);
      state.setTransitions(transitions);
      state.setDescription("desc_1");
      
      PSAssignedRole assignedRole = new PSAssignedRole();
      assignedRole.setStateId(state.getStateId());
      PSAssignedRole assignedRole2 = new PSAssignedRole();
      assignedRole2.fromXML(assignedRole.toXML());
      assertEquals(assignedRole, assignedRole2);
      
      List<PSAssignedRole> assignedRoles = new ArrayList<PSAssignedRole>();
      assignedRoles.add(assignedRole);
      assignedRole2 = new PSAssignedRole();
      assignedRole2.setGUID(new PSGuid(PSTypeEnum.WORKFLOW_ROLE, assignedRole.getGUID().getUUID()+1));
      assignedRoles.add(assignedRole2);
      
      state.setAssignedRoles(assignedRoles);
      PSState state2 = new PSState();
      state2.fromXML(state.toXML());
      assertEquals(state,state2);
      
      List<PSState> states = new ArrayList<PSState>();
      states.add(state);
      states.add(state2);
      
      PSNotificationDef notificationDef = new PSNotificationDef();
      notificationDef.setGUID(new PSGuid(PSTypeEnum.WORKFLOW_NOTIFICATION, 36));
      notificationDef.setSubject("subject_1");
      notificationDef.setBody("body_1");
      PSNotificationDef notificationDef2 = new PSNotificationDef();
      notificationDef2.fromXML(notificationDef.toXML());
      assertEquals(notificationDef, notificationDef2);
      
      List<PSNotificationDef> notificationDefs = 
         new ArrayList<PSNotificationDef>();
      notificationDefs.add(notificationDef);
      notificationDefs.add(notificationDef2);
      
      PSWorkflow wf = new PSWorkflow();
      wf.setGUID(new PSGuid(PSTypeEnum.WORKFLOW, 12));
      wf.setName("name");
      wf.setDescription("description");
      wf.setAdministratorRole("admin");
      wf.setInitialStateId(1);
      wf.setStates(states);
      wf.setRoles(roles);
      wf.setNotificationDefs(notificationDefs);
      wf.setVersion(0);
      System.out.println(wf.toXML());

      PSWorkflow wf2 = new PSWorkflow();
      wf2.fromXML(wf.toXML());
      assertEquals(wf, wf2);
      
      assertEquals(roles, wf.getRoles());
      assertEquals(roleNames, wf.getRoleNames(roleIds));
      assertEquals(roleIds, wf.getRoleIds(roleNames));

   }

   private PSAgingTransition createAgingTransition(int uuId)
   {
      PSAgingTransition agingTransition = new PSAgingTransition();
      agingTransition.setGUID(new PSGuid(PSTypeEnum.WORKFLOW_TRANSITION, uuId));
      agingTransition.setLabel("agingLabel_" + uuId);
      agingTransition.setDescription("agingDesc_" + uuId);
      return agingTransition;
   }

   private PSTransition createTransition(int uuId, List<PSWorkflowRole> roles,
         List<PSNotification> notifications)
   {
      PSTransition transition = new PSTransition();
      transition.setGUID(new PSGuid(PSTypeEnum.WORKFLOW_TRANSITION, uuId));
      transition.setLabel("tranLabel_" + uuId);
      transition.setNotifications(notifications);
      transition.setDescription("tranDesc_" + uuId);
      List<PSTransitionRole> transRoles = new ArrayList<PSTransitionRole>();
      for (PSWorkflowRole wfrole : roles)
      {
         PSTransitionRole transRole = new PSTransitionRole();
         transRole.setWorkflowId(wfrole.getWorkflowId());
         transRole.setTransitionId(transition.getGUID().longValue());
         transRole.setRoleId(wfrole.getGUID().longValue());
         transRoles.add(transRole);
      }
      transition.setTransitionRoles(transRoles);
      return transition;
   }
}


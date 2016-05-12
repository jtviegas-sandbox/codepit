/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.catalina;

import org.apache.catalina.Engine;
import org.apache.catalina.Realm;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.openejb.BeanContext;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.security.AbstractSecurityService;
import org.apache.openejb.spi.CallerPrincipal;
import org.apache.tomee.loader.TomcatHelper;

import javax.security.auth.Subject;
import javax.security.auth.login.CredentialNotFoundException;
import javax.security.auth.login.LoginException;
import javax.security.jacc.EJBMethodPermission;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.AccessControlException;
import java.security.Principal;
import java.util.*;

public class TomcatSecurityService extends AbstractSecurityService {
    static protected final ThreadLocal<LinkedList<Subject>> runAsStack = new ThreadLocal<LinkedList<Subject>>() {
        protected LinkedList<Subject> initialValue() {
            return new LinkedList<Subject>();
        }
    };

    private Realm defaultRealm;

    public TomcatSecurityService() {
        Server server = TomcatHelper.getServer();
        for (Service service : server.findServices()) {
            if (service.getContainer() instanceof Engine) {
                Engine engine = (Engine) service.getContainer();
                if (engine.getRealm() != null) {
                    defaultRealm = engine.getRealm();
                    break;
                }
            }
        }
    }

    @Override
    public boolean isCallerAuthorized(final Method method, final InterfaceType type) {
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        try {
            final BeanContext beanContext = threadContext.getBeanContext();
            final String ejbName = beanContext.getEjbName();
            String name = type == null ? null : type.getSpecName();
            if ("LocalBean".equals(name) || "LocalBeanHome".equals(name)) {
                name = null;
            }
            final Identity currentIdentity = clientIdentity.get();
            final SecurityContext securityContext;
            if(currentIdentity == null) {
                securityContext= threadContext.get(SecurityContext.class);
            } else {
                securityContext = new SecurityContext(currentIdentity.getSubject());
            }
            securityContext.acc.checkPermission(new EJBMethodPermission(ejbName, name, method));
        } catch (AccessControlException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isCallerInRole(final String role) {
        final Principal principal = getCallerPrincipal();
        if (TomcatUser.class.isInstance(principal)) {
            final TomcatUser tomcatUser = (TomcatUser) principal;
            final GenericPrincipal genericPrincipal = (GenericPrincipal) tomcatUser.getTomcatPrincipal();
            final String[] roles = genericPrincipal.getRoles();
            if (roles != null) {
                for (String userRole : roles) {
                    if (userRole.equals(role)) {
                        return true;
                    }
                }
            }
            return false;
        }
        return super.isCallerInRole(role);
    }

    public UUID login(String realmName, String username, String password) throws LoginException {
        if (defaultRealm == null) {
            throw new LoginException("No Tomcat realm available");
        }

        final Principal principal = defaultRealm.authenticate(username, password);
        if (principal == null) throw new CredentialNotFoundException(username);

        final Subject subject = createSubject(defaultRealm, principal);
        return registerSubject(subject);
    }

    private Subject createSubject(Realm realm, Principal principal) {
        final Set<Principal> principals = new HashSet<Principal>();
        principals.add(new TomcatUser(realm, principal));
        return new Subject(true, principals, new HashSet(), new HashSet());
    }

    public Set<String> getLogicalRoles(Principal[] principals, Set<String> logicalRoles) {
        final Set<String> roles = new LinkedHashSet<String>(logicalRoles.size());
        for (String logicalRole : logicalRoles) {
            for (Principal principal : principals) {
                if (principal instanceof TomcatUser) {
                    TomcatUser user = (TomcatUser) principal;
                    if (TomcatHelper.hasRole(user.getRealm(), user.getTomcatPrincipal(), logicalRole)) {
                        roles.add(logicalRole);
                        break;
                    }
                } else if (principal != null) {
                    String name = principal.getName();
                    if (logicalRole.equals(name)) {
                        roles.add(logicalRole);
                    }
                }
            }
        }
        return roles;
    }

    @Override
    public Principal getCallerPrincipal() {
        final Identity currentIdentity = clientIdentity.get();
        if (currentIdentity != null) {
            final Set<Principal> principals = currentIdentity.getSubject().getPrincipals();
            for (final Principal principal : principals) {
                if (principal.getClass().isAnnotationPresent(CallerPrincipal.class)) {
                    return principal;
                }
            }
            if (!principals.isEmpty()) {
                return principals.iterator().next();
            }
        }
        return super.getCallerPrincipal();
    }

    public Object enterWebApp(Realm realm, Principal principal, String runAs) {
        Identity newIdentity = null;
        if (principal != null) {
            Subject newSubject = createSubject(realm, principal);
            newIdentity = new Identity(newSubject, null);
        }

        Identity oldIdentity = clientIdentity.get();
        WebAppState webAppState = new WebAppState(oldIdentity, runAs != null);
        clientIdentity.set(newIdentity);

        if (runAs != null) {
            Subject runAsSubject = createRunAsSubject(runAs);
            runAsStack.get().addFirst(runAsSubject);
        }

        return webAppState;
    }

    public void exitWebApp(Object state) {
        if (state instanceof WebAppState) {
            final WebAppState webAppState = (WebAppState) state;
            if (webAppState.oldIdentity == null) {
                clientIdentity.remove();
            } else {
                clientIdentity.set(webAppState.oldIdentity);
            }

            if (webAppState.hadRunAs) {
                runAsStack.get().removeFirst();
            }
        }
    }

    protected Subject getRunAsSubject(final BeanContext callingBeanContext) {
        final Subject runAsSubject = super.getRunAsSubject(callingBeanContext);
        if (runAsSubject != null) return runAsSubject;

        final LinkedList<Subject> stack = runAsStack.get();
        if (stack.isEmpty()) {
            return null;
        }
        return stack.getFirst();
    }


    protected Subject createRunAsSubject(String role) {
        if (role == null) {
            return null;
        }

        final Set<Principal> principals = new HashSet<Principal>();
        principals.add(new RunAsRole(role));
        return new Subject(true, principals, new HashSet(), new HashSet());
    }

    @CallerPrincipal
    protected static class TomcatUser implements Principal {
        private final Realm realm;
        private final Principal tomcatPrincipal;


        public TomcatUser(Realm realm, Principal tomcatPrincipal) {
            if (realm == null) throw new NullPointerException("realm is null");
            if (tomcatPrincipal == null) throw new NullPointerException("tomcatPrincipal is null");
            this.realm = realm;
            this.tomcatPrincipal = tomcatPrincipal;
        }

        public Realm getRealm() {
            return realm;
        }

        public Principal getTomcatPrincipal() {
            return tomcatPrincipal;
        }

        public String getName() {
            return tomcatPrincipal.getName();
        }

        public String toString() {
            return "[TomcatUser: " + tomcatPrincipal + "]";
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TomcatUser that = (TomcatUser) o;

            return realm.equals(that.realm) && tomcatPrincipal.equals(that.tomcatPrincipal);
        }

        public int hashCode() {
            int result;
            result = realm.hashCode();
            result = 31 * result + tomcatPrincipal.hashCode();
            return result;
        }
    }

    protected static class RunAsRole implements Principal {
        private final String name;

        public RunAsRole(String name) {
            if (name == null) throw new NullPointerException("name is null");
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return "[RunAsRole: " + name + "]";
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RunAsRole runAsRole = (RunAsRole) o;

            return name.equals(runAsRole.name);
        }

        public int hashCode() {
            return name.hashCode();
        }
    }

    private static class WebAppState implements Serializable {
        private final Identity oldIdentity;
        private final boolean hadRunAs;


        public WebAppState(Identity oldIdentity, boolean hadRunAs) {
            this.oldIdentity = oldIdentity;
            this.hadRunAs = hadRunAs;
        }
    }

}

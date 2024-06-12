/* Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jenkins.security;

import static java.util.logging.Level.FINER;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gravity.security.annotations.requirements.Critical;
import org.gravity.security.annotations.requirements.Integrity;
import org.gravity.security.annotations.requirements.Secrecy;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import jenkins.ExtensionFilter;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;

/**
 * Checks if the password given in the BASIC header matches the user's actual password, as opposed
 * to other pseudo-passwords like API tokens.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.576
 */
@Restricted(NoExternalUse.class)
@Extension
@Critical(secrecy = { "BasicHeaderRealPasswordAuthenticator.authenticationDetailsSource:AuthenticationDetailsSource" }, integrity = { "authenticationDetailsSource:AuthenticationDetailsSource" })
public class BasicHeaderRealPasswordAuthenticator extends BasicHeaderAuthenticator {
    @Secrecy
    @Integrity
    private AuthenticationDetailsSource authenticationDetailsSource = new WebAuthenticationDetailsSource();

    @Override
    @Secrecy
    // TODO: Parameter enthaelt req, username und password
    public Authentication authenticate2(HttpServletRequest req, HttpServletResponse rsp, String username, String password) throws IOException, ServletException {
        if (DISABLE) {
            return null;
        }

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(username, password);
        // &begin[use_authDetailsSource]
        authRequest.setDetails(authenticationDetailsSource.buildDetails(req));
        // &end[use_authDetailsSource]

        try {
            // &begin[use_authDetailsSource]
            Authentication a = Jenkins.get().getSecurityRealm().getSecurityComponents().manager2.authenticate(authRequest);
            // &end[use_authDetailsSource]
            // Authentication success
            LOGGER.log(FINER, "Authentication success: {0}", a);
            return a;
        } catch (AuthenticationException failed) {
            // Authentication failed
            LOGGER.log(FINER, "Authentication request for user: {0} failed: {1}", new Object[] { username, failed });
            return null;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(BasicHeaderRealPasswordAuthenticator.class.getName());

    /**
     * Legacy property to disable the real password support. Now that this is an extension,
     * {@link ExtensionFilter} is a better way to control this.
     */
    @SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL", justification = "for script console")
    public static boolean DISABLE = SystemProperties.getBoolean("jenkins.security.ignoreBasicAuth");
}

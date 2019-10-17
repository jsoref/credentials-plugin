/*
 * The MIT License
 *
 * Copyright (c) 2011-2012, CloudBees, Inc., Stephen Connolly.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.cloudbees.plugins.credentials.matchers;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import com.cloudbees.plugins.credentials.common.UsernameCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang.StringEscapeUtils;
import java.util.logging.Logger;

import static java.util.logging.Level.*;

/**
 * Matches credentials that are {@link UsernameCredentials} and have the specified {@link
 * UsernameCredentials#getUsername()}
 *
 * @since 1.5
 */
public class UsernameMatcher implements CredentialsMatcher, CredentialsMatcher.CQL {
    /**
     * Standardize serialization.
     *
     * @since 2.1.0
     */
    private static final long serialVersionUID = -2166795904091485580L;

    private static final Logger LOGGER = Logger.getLogger(UsernameMatcher.class.getName());

    /**
     * The username to match.
     */
    @NonNull
    private final String username;

    /**
     * Constructs a new instance.
     *
     * @param username the username to match.
     */
    public UsernameMatcher(@NonNull String username) {
        username.getClass(); // throw NPE if null
        this.username = username;
    }

    /**
     * {@inheritDoc}
     */
    public boolean matches(@NonNull Credentials item) {
        String self = toString();
        LOGGER.log(WARNING, "{0} [item {1}, item instanceof UsernameCredentials {2}, username.equals(((IdCredentials) item).getUsername()) {3}]",
                new Object[]{self, item.toString(), item instanceof UsernameCredentials, username.equals(((UsernameCredentials) item).getUsername())});
        return item instanceof UsernameCredentials && username.equals(((UsernameCredentials) item).getUsername());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String describe() {
        String self = toString();
        LOGGER.log(WARNING, "{0} describe: {1}",
                new Object[]{self, String.format("(username == \"%s\")", StringEscapeUtils.escapeJava(username))});
        return String.format("(username == \"%s\")", StringEscapeUtils.escapeJava(username));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return username.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UsernameMatcher that = (UsernameMatcher) o;

        return username.equals(that.username);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UsernameMatcher{");
        sb.append("username='").append(username).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

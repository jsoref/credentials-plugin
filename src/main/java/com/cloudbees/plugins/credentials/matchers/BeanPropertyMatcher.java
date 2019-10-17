/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc., Stephen Connolly.
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
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.logging.Logger;

import static java.util.logging.Level.*;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Matches credentials that have a Java Bean property with an expected value.
 *
 * @since 2.1.0
 */
public class BeanPropertyMatcher<T extends Serializable> implements CredentialsMatcher, CredentialsMatcher.CQL {
    /**
     * Standardize serialization.
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(BeanPropertyMatcher.class.getName());

    /**
     * The property name.
     */
    @NonNull
    private final String name;
    /**
     * The expected value.
     */
    @CheckForNull
    private final T expected;

    /**
     * Constructs an instance that matches the specified java bean property against the supplied value.
     *
     * @param name     the property name.
     * @param expected the expected value.
     */
    public BeanPropertyMatcher(@NonNull String name, @CheckForNull T expected) {
        this.name = name;
        this.expected = expected;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String describe() {
        String description;
        if (expected == null) {
            description = String.format("(%s == null)", name);
        } else if (expected instanceof String) {
            description = String.format("(%s == \"%s\")", name, StringEscapeUtils.escapeJava((String) expected));
        } else if (expected instanceof Character) {
            description = String.format("(%s == \'%s\')", name, StringEscapeUtils.escapeJava(expected.toString()));
        } else if (expected instanceof Number) {
            description = String.format("(%s == %s)", name, expected);
        } else if (expected instanceof Boolean) {
            description = expected.toString();
        } else if (expected instanceof Enum) {
            description = String.format("(%s == %s.%s)", name, ((Enum) expected).getDeclaringClass().getName(), ((Enum) expected).name());
        } else {
            description = null; // we cannot describe the expected value in CQL
        }
        LOGGER.log(WARNING, "{0}.describe: {1}", new Object[]{toString(), description});
        return description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(@NonNull Credentials item) {
        String self = toString();
        LOGGER.log(FINEST, "{0} matches(item {1})", new Object[] {self, item.toString()});

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(item.getClass());
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                if (name.equals(pd.getName())) {
                    Method readMethod = pd.getReadMethod();
                    if (readMethod == null) {
                        LOGGER.log(FINE, "{0} matches(item {1}) readMethod = null -> false", new Object[] {self, item.toString()});
                        return false; // we cannot read it therefore it cannot be a match
                    }
                    try {
                        Object actual = readMethod.invoke(item);
                        LOGGER.log(FINE, "{0} matches(item {1}) expected {2}: readMethod: {3}; equals: {4}", new Object[] {self, item.toString(), expected, actual, Objects.equals(expected, actual)});
                        return Objects.equals(expected, actual);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        LOGGER.log(WARNING, "{0} matches(item {1}) exception {2}", new Object[] {self, item.toString(), e});
                        return false; // if we cannot access it then it's not a match
                    }
                }
            }
            LOGGER.log(WARNING, "{0} matches(item {1}): false", new Object[] {self, item.toString()});
            return false; // if there is no corresponding property then it cannot be a match
        } catch (IntrospectionException e) {
            LOGGER.log(WARNING, "{0} matches(item {1}): exception {2}", new Object[] {self, item.toString(), e});
            return false; // if we cannot introspect it then it cannot be a match
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (expected != null ? expected.hashCode() : 0);
        return result;
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

        BeanPropertyMatcher<?> that = (BeanPropertyMatcher<?>) o;

        if (!name.equals(that.name)) {
            return false;
        }
        return Objects.equals(expected, that.expected);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BeanPropertyMatcher{");
        sb.append("name='").append(name).append('\'');
        sb.append(", expected=").append(expected);
        sb.append('}');
        return sb.toString();
    }
}

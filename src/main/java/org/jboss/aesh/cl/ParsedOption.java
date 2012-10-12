/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.aesh.cl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class ParsedOption {

    private String name;
    private String longName;
    private List<String> values;
    private List<OptionProperty> properties;

    public ParsedOption(String name, String longName, List<String> values) {
        this.name = name;
        this.longName = longName;
        this.values = values;
    }

    public ParsedOption(String name, String longName, OptionProperty property) {
        this.name = name;
        this.longName = longName;
        values = new ArrayList<String>();
        properties = new ArrayList<OptionProperty>();
        properties.add(property);
    }

    public ParsedOption(String name, String longName, String value) {
        this.name = name;
        this.longName = longName;
        values = new ArrayList<String>();
        values.add(value);
    }

    public String getName() {
        return name;
    }

    public String getLongName() {
        return longName;
    }

    public String getValue() {
        return values.get(0);
    }

    public List<String> getValues() {
        return values;
    }

    public List<OptionProperty> getProperties() {
        return properties;
    }



}
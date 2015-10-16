/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.columnSeparators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;


public class PipeSeparator extends ALineSeparator {

    private static final SeparatorType TYPE = SeparatorType.PIPE;
    private static final String REGEXP_DO_NOT_CONSUME_WHEN_WHITESPACE_SHOULD_BELONGS_TO_NEXT_PIPE = "((?=\\s+[|])|\\s+)";
    private static final Pattern PIPE_SEPARATOR = Pattern.compile("(^[ ]?[|]"
            + REGEXP_DO_NOT_CONSUME_WHEN_WHITESPACE_SHOULD_BELONGS_TO_NEXT_PIPE
            + ")|" + "(\\s+[|]"
            + REGEXP_DO_NOT_CONSUME_WHEN_WHITESPACE_SHOULD_BELONGS_TO_NEXT_PIPE
            + ")|" + "((\\s)+[|](\\s)+$)|" + "((\\s)+[|]$)|" + "(^[|]$)");

    private final Matcher matcher;


    public PipeSeparator(int lineNumber, String line) {
        super(lineNumber, line);
        this.matcher = PIPE_SEPARATOR.matcher(line);
    }


    @Override
    protected Separator nextSeparator() {
        int start = matcher.start();
        int end = matcher.end();
        Separator s = new Separator();
        s.setType(TYPE);
        s.setStartColumn(start);
        s.setText(new StringBuilder().append(line.substring(start, end)));
        s.setRaw(new StringBuilder(s.getText()));
        s.setLineNumber(getLineNumber());

        return s;
    }


    @Override
    protected boolean hasNextSeparator() {
        return matcher.find();
    }


    public SeparatorType getProducedType() {
        return TYPE;
    }
}

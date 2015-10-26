/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.userKeywords.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.mapping.ParsingStateHelper;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.core.testData.text.read.recognizer.executables.RobotSpecialTokens;


public class KeywordExecutableRowArgumentMapper implements IParsingMapper {

    private final ParsingStateHelper stateHelper;
    private final KeywordFinder keywordFinder;
    private final RobotSpecialTokens specialTokensRecognizer;


    public KeywordExecutableRowArgumentMapper() {
        this.stateHelper = new ParsingStateHelper();
        this.keywordFinder = new KeywordFinder();
        this.specialTokensRecognizer = new RobotSpecialTokens();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        UserKeyword keyword = keywordFinder.findOrCreateNearestKeyword(
                currentLine, processingState, robotFileOutput, rt, fp);
        List<IRobotTokenType> types = rt.getTypes();
        types.add(0, RobotTokenType.KEYWORD_ACTION_ARGUMENT);

        List<RobotToken> specialTokens = specialTokensRecognizer.recognize(fp,
                text);
        for (RobotToken token : specialTokens) {
            types.addAll(token.getTypes());
        }

        List<RobotExecutableRow<UserKeyword>> keywordExecutionRows = keyword
                .getKeywordExecutionRows();
        RobotExecutableRow<UserKeyword> robotExecutableRow = keywordExecutionRows
                .get(keywordExecutionRows.size() - 1);
        robotExecutableRow.addArgument(rt);

        processingState.push(ParsingState.KEYWORD_INSIDE_ACTION_ARGUMENT);
        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;
        ParsingState state = stateHelper.getCurrentStatus(processingState);
        result = (state == ParsingState.KEYWORD_INSIDE_ACTION || state == ParsingState.KEYWORD_INSIDE_ACTION_ARGUMENT);

        return result;
    }
}

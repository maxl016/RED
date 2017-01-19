/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IMarkerResolution;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.ChangeToFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.CreateLocalVariableFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.CreateVariableFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.RemoveVariableFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.RemoveWhitespacesFromVariableNameFixer;


public enum VariablesProblem implements IProblemCause {
    DUPLICATED_VARIABLE {
        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.DUPLICATED_VARIABLE;
        }

        @Override
        public String getProblemDescription() {
            return "Duplicated variable definition '%s'";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new RemoveVariableFixer(marker.getAttribute(AdditionalMarkerAttributes.NAME, null)));
        }
    },
    INVALID_TYPE {
        @Override
        public String getProblemDescription() {
            return "Invalid variable definition '%s'. Unable to recognize variable type";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new RemoveVariableFixer(marker.getAttribute(AdditionalMarkerAttributes.NAME, null)));
        }
    },
    INVALID_NAME {
        @Override
        public String getProblemDescription() {
            return "Invalid variable name '%s'. Name can't contain whitespaces after the type identificator";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new RemoveWhitespacesFromVariableNameFixer(
                    marker.getAttribute(AdditionalMarkerAttributes.NAME, null)));
        }
    },
    UNDECLARED_VARIABLE_USE {
        @Override
        public String getProblemDescription() {
            return "Variable '%s' is used, but not defined";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            final IFile suiteFile = (IFile) marker.getResource();
            final IRegion problemRegion = RobotProblem.getRegionOf(marker);
            final String varName = marker.getAttribute(AdditionalMarkerAttributes.NAME, "");
            final boolean defineLocally = marker.getAttribute(AdditionalMarkerAttributes.DEFINE_VAR_LOCALLY, false);

            final ArrayList<IMarkerResolution> fixers = new ArrayList<>();
            if (defineLocally) {
                fixers.add(new CreateLocalVariableFixer(varName));
            }
            fixers.add(new CreateVariableFixer(varName));
            fixers.addAll(ChangeToFixer.createFixers(problemRegion, new SimilaritiesAnalyst()
                    .provideSimilarAccessibleVariables(suiteFile, problemRegion.getOffset(), varName)));
            return fixers;
        }
    },
    INVALID_DICTIONARY_ELEMENT_SYNTAX {

        @Override
        public String getProblemDescription() {
            return "Item '%s' in dictionary '%s' have to contain '=' separator";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            final IRegion problemRegion = RobotProblem.getRegionOf(marker);
            final String value = marker.getAttribute(AdditionalMarkerAttributes.VALUE, "");

            final ArrayList<IMarkerResolution> fixers = new ArrayList<>();
            fixers.addAll(ChangeToFixer.createFixers(problemRegion, newArrayList(value + "=value")));
            return fixers;
        }
    },
    DICTIONARY_NOT_AVAILABLE {
        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.UNSUPPORTED_API;
        }

        @Override
        public String getProblemDescription() {
            return "Invalid variable definition '%s'. Dictionary type is available since Robot Framework 2.9.x version";
        }

        @Override
        public boolean hasResolution() {
            return false;
        }
    },
    SCALAR_WITH_MULTIPLE_VALUES_2_7 {
        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.INCORRECT_INITIALIZATION;
        }

        @Override
        public String getProblemDescription() {
            return "Scalar variable '%s' is initialized with list value";
        }

        @Override
        public boolean hasResolution() {
            return false;
        }
    },
    SCALAR_WITH_MULTIPLE_VALUES_2_8_x {
        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.UNSUPPORTED_API;
        }

        @Override
        public String getProblemDescription() {
            return "Invalid variable definition '%s'. Scalar variable cannot have multiple value in RobotFramework 2.8.x";
        }

        @Override
        public boolean hasResolution() {
            return false;
        }
    };

    @Override
    public boolean hasResolution() {
        return true;
    }

    @Override
    public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
        return newArrayList();
    }

    @Override
    public ProblemCategory getProblemCategory() {
        return ProblemCategory.RUNTIME_ERROR;
    }

    @Override
    public String getEnumClassName() {
        return VariablesProblem.class.getName();
    }
}

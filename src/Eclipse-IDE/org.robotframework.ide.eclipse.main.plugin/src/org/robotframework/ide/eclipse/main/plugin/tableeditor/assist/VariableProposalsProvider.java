/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.text.IRegion;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.swt.widgets.Text;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposalPredicate;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposalPredicates;
import org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.jface.assist.RedContentProposal.ModificationStrategy;
import org.robotframework.red.jface.assist.RedContentProposalProvider;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

import com.google.common.base.Optional;

public class VariableProposalsProvider implements RedContentProposalProvider {

    private final RobotSuiteFile suiteFile;

    private final IRowDataProvider<?> dataProvider;

    public VariableProposalsProvider(final RobotSuiteFile suiteFile, final IRowDataProvider<?> dataProvider) {
        this.suiteFile = suiteFile;
        this.dataProvider = dataProvider;
    }

    @Override
    public RedContentProposal[] getProposals(final String contents, final int position,
            final AssistantContext context) {
        final Optional<IRegion> varRegion = DocumentUtilities.findLiveVariable(contents, position);
        final String prefix = varRegion.isPresent() ? contents.substring(varRegion.get().getOffset(), position) : "";

        final NatTableAssistantContext tableContext = (NatTableAssistantContext) context;
        final AssistProposalPredicate<String> predicate = createGlobalVarPredicate(tableContext.getRow());
        final List<? extends AssistProposal> variableEntities = new RedVariableProposals(suiteFile, predicate)
                .getVariableProposals(prefix, getModelElement(tableContext));

        final List<IContentProposal> proposals = newArrayList();
        for (final AssistProposal proposedVariable : variableEntities) {

            final Optional<ModificationStrategy> modificationStrategy = Optional
                    .<ModificationStrategy> of(new VariableTextModificationStrategy());
            proposals.add(new AssistProposalAdapter(proposedVariable, modificationStrategy));
        }
        return proposals.toArray(new RedContentProposal[0]);
    }

    private RobotFileInternalElement getModelElement(final NatTableAssistantContext tableContext) {
        final Object rowElement = dataProvider.getRowObject(tableContext.getRow());
        if (rowElement instanceof RobotFileInternalElement) {
            return (RobotFileInternalElement) rowElement;
        } else if (rowElement instanceof Entry) {
            final RobotFileInternalElement element = (RobotFileInternalElement) ((Entry<?, ?>) rowElement).getValue();
            return element != null ? element : suiteFile.findSection(RobotSettingsSection.class).get();
        }
        throw new IllegalStateException("Unrecognized element in table");
    }

    private AssistProposalPredicate<String> createGlobalVarPredicate(final int row) {
        final Object element = dataProvider.getRowObject(row);
        return element instanceof RobotElement
                ? AssistProposalPredicates.globalVariablePredicate((RobotElement) element)
                : AssistProposalPredicates.<String> alwaysTrue();
    }

    private static class VariableTextModificationStrategy implements ModificationStrategy {

        @Override
        public void insert(final Text text, final IContentProposal proposal) {
            final String content = proposal.getContent();

            final String currentTextBeforeSelection = text.getText().substring(0, text.getSelection().x);
            final String currentTextAfterSelection = text.getText().substring(text.getSelection().x);

            int maxCommon = 0;
            for (int i = 0; i < content.length() && i <= currentTextBeforeSelection.length(); i++) {
                final String currentSuffix = currentTextBeforeSelection
                        .substring(currentTextBeforeSelection.length() - i, currentTextBeforeSelection.length());
                final String toInsertPrefix = content.substring(0, i);

                if (currentSuffix.equalsIgnoreCase(toInsertPrefix)) {
                    maxCommon = toInsertPrefix.length();
                }
            }

            String toInsert = currentTextBeforeSelection.substring(0, currentTextBeforeSelection.length() - maxCommon)
                    + content;
            final int newSelection = toInsert.length();
            toInsert += findSuffix(currentTextAfterSelection);

            text.setText(toInsert);
            text.setSelection(newSelection);
        }

        private String findSuffix(final String currentTextAfterSelection) {
            for (int i = 0; i < currentTextAfterSelection.length(); i++) {
                if (currentTextAfterSelection.charAt(i) == '}') {
                    return i == currentTextAfterSelection.length() - 1 ? ""
                            : currentTextAfterSelection.substring(i + 1);
                }
            }
            return currentTextAfterSelection;
        }
    }
}

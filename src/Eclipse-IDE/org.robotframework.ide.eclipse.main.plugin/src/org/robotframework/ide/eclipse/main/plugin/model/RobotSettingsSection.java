package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.robotframework.ide.core.testData.model.AKeywordBaseSetting;
import org.robotframework.ide.core.testData.model.ATags;
import org.robotframework.ide.core.testData.model.table.ARobotSectionTable;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.model.table.setting.LibraryImport;
import org.robotframework.ide.core.testData.model.table.setting.Metadata;
import org.robotframework.ide.core.testData.model.table.setting.ResourceImport;
import org.robotframework.ide.core.testData.model.table.setting.SuiteDocumentation;
import org.robotframework.ide.core.testData.model.table.setting.TestTemplate;
import org.robotframework.ide.core.testData.model.table.setting.TestTimeout;
import org.robotframework.ide.core.testData.model.table.setting.VariablesImport;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class RobotSettingsSection extends RobotSuiteFileSection implements IRobotCodeHoldingElement {

    public static final String SECTION_NAME = "Settings";

    RobotSettingsSection(final RobotSuiteFile parent) {
        super(parent, SECTION_NAME);
    }

    public RobotSetting createSetting(final String name, final String comment, final String... args) {
        RobotSetting setting;
        if (name.equals(SettingsGroup.METADATA.getName())) {
            setting = new RobotSetting(this, SettingsGroup.METADATA, name, newArrayList(args), comment);
        } else if (name.equals(SettingsGroup.LIBRARIES.getName())) {
            setting = new RobotSetting(this, SettingsGroup.LIBRARIES, name, newArrayList(args), comment);
        } else if (name.equals(SettingsGroup.RESOURCES.getName())) {
            setting = new RobotSetting(this, SettingsGroup.RESOURCES, name, newArrayList(args), comment);
        } else if (name.equals(SettingsGroup.VARIABLES.getName())) {
            setting = new RobotSetting(this, SettingsGroup.VARIABLES, name, newArrayList(args), comment);
        } else {
            setting = new RobotSetting(this, name, newArrayList(args), comment);
        }
        elements.add(setting);
        return setting;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<RobotKeywordCall> getChildren() {
        return (List<RobotKeywordCall>) super.getChildren();
    }

    public List<RobotKeywordCall> getMetadataSettings() {
        return getSettingsFromGroup(SettingsGroup.METADATA);
    }

    public List<RobotKeywordCall> getResourcesSettings() {
        return getSettingsFromGroup(SettingsGroup.RESOURCES);
    }
    
    public List<RobotKeywordCall> getVariablesSettings() {
        return getSettingsFromGroup(SettingsGroup.VARIABLES);
    }

    public List<RobotKeywordCall> getImportSettings() {
        return newArrayList(Iterables.filter(getChildren(), new Predicate<RobotKeywordCall>() {
            @Override
            public boolean apply(final RobotKeywordCall element) {
                return SettingsGroup.getImportsGroupsSet()
                                .contains((((RobotSetting) element).getGroup()));
            }
        }));
    }

    private List<RobotKeywordCall> getSettingsFromGroup(final SettingsGroup group) {
        return newArrayList(Iterables.filter(getChildren(), new Predicate<RobotKeywordCall>() {
            @Override
            public boolean apply(final RobotKeywordCall element) {
                return (((RobotSetting) element).getGroup() == group);
            }
        }));
    }

    public RobotSetting getSetting(final String name) {
        for (final RobotKeywordCall setting : getChildren()) {
            if (name.equals(setting.getName())) {
                return (RobotSetting) setting;
            }
        }
        return null;
    }

    public List<IPath> getResourcesPaths() {
        final List<RobotKeywordCall> resources = getResourcesSettings();
        final List<IPath> paths = newArrayList();
        for (final RobotElement element : resources) {
            final RobotSetting setting = (RobotSetting) element;
            final List<String> args = setting.getArguments();
            if (!args.isEmpty()) {
                paths.add(new org.eclipse.core.runtime.Path(args.get(0)));
            }
        }
        return paths;
    }

    @Override
    public void link(final ARobotSectionTable table) {
        final SettingTable settingsTable = (SettingTable) table;
        
        for (final Metadata metadataSetting : settingsTable.getMetadatas()) {
            final String name = metadataSetting.getDeclaration().getText().toString();
            final List<String> args = newArrayList(metadataSetting.getKey().getText().toString());
            args.addAll(Lists.transform(metadataSetting.getValues(), TokenFunctions.tokenToString()));
            elements.add(new RobotSetting(this, SettingsGroup.METADATA, name, args, ""));
        }
        for (final AImported importSetting : settingsTable.getImports()) {
            if (importSetting instanceof LibraryImport) {

                final LibraryImport libraryImport = (LibraryImport) importSetting;

                final String name = libraryImport.getDeclaration().getText().toString();
                final List<String> args = newArrayList(libraryImport.getPathOrName().getText().toString());
                args.addAll(Lists.transform(libraryImport.getArguments(), TokenFunctions.tokenToString()));

                elements.add(new RobotSetting(this, SettingsGroup.LIBRARIES, name, args, ""));
            } else if (importSetting instanceof ResourceImport) {

                final ResourceImport resourceImport = (ResourceImport) importSetting;

                final String name = resourceImport.getDeclaration().getText().toString();
                final List<String> args = newArrayList(resourceImport.getPathOrName().getText().toString());

                elements.add(new RobotSetting(this, SettingsGroup.RESOURCES, name, args, ""));
            } else if (importSetting instanceof VariablesImport) {

                final VariablesImport variablesImport = (VariablesImport) importSetting;

                final String name = variablesImport.getDeclaration().getText().toString();
                final List<String> args = newArrayList(variablesImport.getPathOrName().getText().toString());
                args.addAll(Lists.transform(variablesImport.getArguments(), TokenFunctions.tokenToString()));

                elements.add(new RobotSetting(this, SettingsGroup.VARIABLES, name, args, ""));
            }
        }
        for (final SuiteDocumentation documentationSetting : settingsTable.getDocumentation()) {
            final String name = documentationSetting.getDeclaration().getText().toString();
            final List<String> args = newArrayList(
                    Lists.transform(documentationSetting.getDocumentationText(), TokenFunctions.tokenToString()));
            elements.add(new RobotSetting(this, name, args, ""));
        }
        for (final AKeywordBaseSetting keywordSetting : getKeywordBasedSettings(settingsTable)) {
            final String name = keywordSetting.getDeclaration().getText().toString();
            final List<String> args = newArrayList(keywordSetting.getKeywordName().getText().toString());
            args.addAll(Lists.transform(keywordSetting.getArguments(), TokenFunctions.tokenToString()));
            elements.add(new RobotSetting(this, name, args, ""));
        }
        for (final ATags tagSetting : getTagsSettings(settingsTable)) {
            final String name = tagSetting.getDeclaration().getText().toString();
            final List<String> args = newArrayList(
                    Lists.transform(tagSetting.getTags(), TokenFunctions.tokenToString()));
            elements.add(new RobotSetting(this, name, args, ""));
        }
        for (final TestTemplate templateSetting : settingsTable.getTestTemplates()) {
            final String name = templateSetting.getDeclaration().getText().toString();
            final List<String> args = newArrayList(templateSetting.getKeywordName().getText().toString());
            elements.add(new RobotSetting(this, name, args, ""));
        }
        for (final TestTimeout timeoutSetting : settingsTable.getTestTimeouts()) {
            final String name = timeoutSetting.getDeclaration().getText().toString();
            final List<String> args = newArrayList(timeoutSetting.getTimeout().getText().toString());
            args.addAll(Lists.transform(timeoutSetting.getMessageArguments(), TokenFunctions.tokenToString()));
            elements.add(new RobotSetting(this, name, args, ""));
        }
    }

    private static List<? extends AKeywordBaseSetting> getKeywordBasedSettings(final SettingTable settingTable) {
        final List<AKeywordBaseSetting> elements = newArrayList();
        elements.addAll(settingTable.getSuiteSetups());
        elements.addAll(settingTable.getSuiteTeardowns());
        elements.addAll(settingTable.getTestSetups());
        elements.addAll(settingTable.getTestTeardowns());
        return elements;
    }

    private static List<? extends ATags> getTagsSettings(final SettingTable settingTable) {
        final List<ATags> elements = newArrayList();
        elements.addAll(settingTable.getForceTags());
        elements.addAll(settingTable.getDefaultTags());
        return elements;
    }
}

# robot-runner-plugin

![Build](https://github.com/rey5137/robot-runner-plugin/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/16424-robot-runner.svg)](https://plugins.jetbrains.com/plugin/16424-robot-runner)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/16424-robot-runner.svg)](https://plugins.jetbrains.com/plugin/16424-robot-runner)

## Template ToDo list
- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [x] Verify the [pluginGroup](/gradle.properties), [plugin ID](/src/main/resources/META-INF/plugin.xml) and [sources package](/src/main/kotlin).
- [x] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html).
- [x] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate) for the first time.
- [x] Set the Plugin ID in the above README badges.
- [ ] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified about releases containing new features and fixes.

<!-- Plugin description -->
Run Configuration to run Robot Framework test suites

## Change log

### 0.6.0
#### Fixed
- Fix UI bug on 2023.3 IntelliJ version

### 0.5.0
#### Fixed
- Fix UI bug on new IntelliJ versions


### 0.4.6
#### Fixed
- Bug cannot open file in Assigment table

### 0.4.5
#### Added
- Support End step keyword
- Support step folding in robot script

### 0.4.4
#### Added
- Support nested Step keyword
- Support new output format of Robot 6.0

### 0.4.3
#### Added
- Support grouping keywords by RobotStepLibrary.Step keyword

### 0.4.2
#### Added
- Gutter icon to run single testcase (support Intellibot #patched plugin)

### 0.4.1
#### Fixed
- Bug error when click run testcase icon

### 0.4.0
#### Added
- Gutter icon to run single testcase (support Intellibot plugin)
- Execution option to run with pabot
- Action to merge all output result files


## How to

### New Robot Run Configuration

- Open Edit Run Configurations dialog.

- Click Add New Configuration button (plus icon at the top left panel).

- Choose Robot Runner option.

- Choose a Python interpreter from dropdown list (you must setup Python SDK beforehand).

- Setup other options as you want.

- Click Apply button.

### Setup Configuration to use with "Run Robot ..." menu

- Open Settings/Preferences > Tools > Robot Runner Settings.

- Tick checkbox at the column "Test Suite" / "Test Case" to select which configuration will be shown in "Run Robot ..." menu.

### View Robot result 

- Open Robot's output.xml file in IntelliJ

- Choose the tab "Robot result"

### View file in Argument/Assignment table

- In Value column of Argument/Assignment table, if a value is a file path, then an open file icon will be shown in left size of cell.

- Click the open file icon to view file.

### Show output view in run configuration tab

- Choose option "Show output view" in Run Configuration setting.


<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "robot-runner-plugin"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/rey5137/robot-runner-plugin/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template

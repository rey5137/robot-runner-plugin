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

- Tick checkbox at column "Test Suite" / "Test Case" to select which configuration will be shown in "Run Robot ..." menu.

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

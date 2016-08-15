# ACE Importer for IntelliJ IDEA

_Naive IntelliJ plugin that sends current file to the ACE import endpoint._

### Installation

- IntelliJ -> Preferences -> Plugins -> Install plugin from disk -> `${LOCATION}/ace-importer.zip` -> Restart

### Usage

- Rightclick a file in the project navigation pane and select "Send file to ACE", or press `^ + ⌥ + ⌘ + I` when in an editor or files list.

### Known limitations

- The plugin is hard coded to use `http://admin:123456@localhost:8080/ace/...`
- There is little to no error handling in place. Expect error messages to pop up in the GUI if the import fails for whatever reason — if you try to import something that is not valid content json, ACE is offline, etc.

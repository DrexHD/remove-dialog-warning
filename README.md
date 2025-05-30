# remove-dialog-warning

A simple server side fabric mod to remove the "Confirm Command Execution" warning screen.

## How?

Any `run_command` dialog actions are converted to `dynamic/custom`, when they are sent to players. When the server
receives this action it runs the specified command as the player, bypassing the warning screen.
# The Sliide KMP "UX Innovator" Challenge

## Architecture choices

Building blocks: Decompose components (BLoC pattern). The rest is simplified clean architecture.

## AI approach

Claude Code setup
- Following the latest best practices: https://github.com/shanraisshan/claude-code-best-practice
- Simple `CLAUDE.md`: architecture, references to the docs.
- Guardrails: hooks verifying Android and iOS build as well as all tests pass.
- Google Stitch MCP for fetching designs.

Execution
- Test the API using `curl`
- Implement the API layer
- Prototype design of the screens in [Stitch](https://stitch.withgoogle.com)
- Implement screens/features one by one
  - Starting with plan mode
  - TDD where possible
  - Testing manually on device
  - Code review as well as `/simplify`

## Setting up the project
Add the GoRest API token to `local.properties`:
```
GO_REST_TOKEN=<token>
GO_REST_TOKEN_FOR_INTEGRATION_TESTS=<token>
```
And to the `iosApp/Configuration/Config.local.xcconfig`:
```
GO_REST_TOKEN=<token>
```

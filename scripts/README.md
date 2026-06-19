# Local Helper Scripts

These scripts wrap common validation flows so contributors do not need to remember long Maven command lines.

## Consumer Smoke

Validate that Pepenium can be consumed from a standalone Maven project:

```powershell
.\scripts\Test-ConsumerSmoke.ps1
```

On Bash-compatible shells:

```bash
./scripts/test-consumer-smoke.sh
```

Useful options:

- PowerShell: `-PepeniumVersion 0.9.7` validates a specific installed or resolved version.
- PowerShell: `-SkipInstall` skips installing local framework artifacts first.
- Bash: `--version 0.9.7` validates a specific installed or resolved version.
- Bash: `--skip-install` skips installing local framework artifacts first.

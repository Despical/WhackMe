# Contributing

When contributing to this repository, please first discuss the change you wish
to make via an [issue](https://github.com/Despical/WhackMe/issues/new) with the
owner of this repository before making a change.

Please note that we have a code of conduct. Follow it in all your interactions
with the project.

## Development Requirements

* Java 25
* The included Gradle wrapper
* A Paper-compatible server when runtime testing is required

Run the complete local verification before submitting a pull request:

```bash
./gradlew clean build javadoc
```

On Windows:

```cmd
gradlew.bat clean build javadoc
```

## Pull Request Process

If you want to help this project and do not know where to start, check the
[currently opened issues](https://github.com/Despical/WhackMe/issues) before
creating a pull request.

* Use spaces rather than tabs for indentation.
* Respect the established code style and package architecture.
* Do not increase version numbers in build files, example configuration files,
  or the README unless the pull request is specifically a release update.
* Keep diffs minimal. Disable automatic reformatting and import organization for
  files unrelated to your change.
* Put broad formatting or cleanup work in a separate pull request.
* Update configuration examples and public API documentation when behavior
  exposed to server owners or integrations changes.

## Issue Process

Use the provided bug report or feature request template when opening an issue.

* Ensure the issue is not a duplicate.
* Keep the issue tracker for reproducible bugs and code-level feature requests,
  not general server support.
* Reproduce the problem on the latest version when possible.
* Include the plugin version, server software, relevant configuration, and logs.
* Open a separate issue instead of posting an unrelated problem under an
  existing issue.

Security vulnerabilities must be reported according to
[SECURITY.md](SECURITY.md), not through a public issue.

## Additional Resources

* [General GitHub documentation](https://docs.github.com/)
* [GitHub pull request documentation](https://docs.github.com/pull-requests)

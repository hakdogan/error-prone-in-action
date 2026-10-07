# Error Prone in Action: Custom Checks and Refaster

A small Maven project that shows how [Error Prone](https://errorprone.info/) catches bugs at compile
time, and how to extend it with your own rules. It is the companion code for the article
*Introduction to Error Prone: How to catch bugs at compile time*.

The Java compiler only checks that code follows the language rules. Error Prone plugs into `javac`
and uses its type information to flag code that compiles but is almost certainly wrong. This repo
covers the three ways to put it to work:

- **Built-in checks**: well-known bugs that Error Prone stops out of the box.
- **Custom BugCheckers**: team-specific rules written against the Error Prone API, with tests.
- **Refaster**: a before/after template that rewrites code across the codebase.

## Requirements

- JDK 21
- Maven 3.9 or later

Error Prone needs access to `javac` internals. Maven compiles in-process, so the required
`--add-exports` / `--add-opens` flags live in [`.mvn/jvm.config`](.mvn/jvm.config) and are picked up
automatically.

## Modules

| Module | Contents |
| --- | --- |
| [`custom-checks`](custom-checks) | Three custom BugCheckers and their tests |
| [`refaster-rules`](refaster-rules) | The `StringIsEmpty` Refaster rule, compiled into `target/rules.refaster` |
| [`demo`](demo) | Code that triggers each check, compiled with Error Prone and the custom checks |

### Custom checks

| Check | Flags | Severity |
| --- | --- | --- |
| `SensitiveDataLogging` | Log calls that would write data marked `@SensitiveData` (national ids, card numbers, emails), directly or through `toString()` | ERROR |
| `BigDecimalDoubleConstructor` | `new BigDecimal(double)`; suggests `BigDecimal.valueOf(double)` as a fix | WARNING |
| `NoSystemOut` | Any use of `System.out` or `System.err` | WARNING |

### Demo classes

| Class | Shows |
| --- | --- |
| `ShortSet` | The `Set<Short>` example from Error Prone's documentation, stopped by the built-in `CollectionIncompatibleType` check |
| `ReportDate` | A `YYYY-MM-dd` date pattern that names 2025-12-29 as 2026, stopped by the built-in `MisusedWeekYear` check |
| `CheckoutService` | `SensitiveDataLogging` |
| `Invoice` | `BigDecimalDoubleConstructor` and `NoSystemOut` |
| `CustomerValidator` | The code the `StringIsEmpty` Refaster rule rewrites |

## Usage

### Build everything

```sh
mvn clean verify
```

The build **fails on purpose** in the `demo` module, where two built-in checks stop code that
compiles but is wrong:

- `ShortSet` passes an `int` to `Set<Short>.remove`, which removes nothing.
- `ReportDate` formats dates with `YYYY`, the week-based year, instead of `yyyy`, the calendar year.

```
[ERROR] ReportDate.java:[16,83] [MisusedWeekYear] Use of "YYYY" (week year) in a date pattern without "ww" (week in year). You probably meant to use "yyyy" (year) instead.
  Did you mean 'private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");'?
[ERROR] ShortSet.java:[22,21] [CollectionIncompatibleType] Argument 'i - 1' should not be passed to this method; its type int is not compatible with its collection's type argument Short
```

The custom checks report on the other demo classes in the same build. Casting the argument to
`short` (`s.remove((short) (i - 1))`) and using `yyyy-MM-dd` makes the build pass.

To build and test only the checks and the Refaster rule:

```sh
mvn clean verify -pl custom-checks,refaster-rules
```

### Make sensitive data logging fail the build

`SensitiveDataLogging` is an ERROR, but the demo lowers it to a warning by default so that
`CheckoutService` does not break the build. The `sensitive-data` profile restores it:

```sh
mvn clean verify -Psensitive-data
```

```
[ERROR] CheckoutService.java:[25,67] [SensitiveDataLogging] 'customer' contains sensitive data and must not be written to logs
[ERROR] CheckoutService.java:[28,90] [SensitiveDataLogging] 'customer.email()' contains sensitive data and must not be written to logs
```

### Apply the Refaster rule

```sh
mvn clean verify -Prefaster
```

This rewrites `demo` sources **in place**. In `CustomerValidator`, `s.length() == 0` becomes
`s.isEmpty()` for every `String`; the `StringBuilder` case is left alone because the rule matches by
type. In this mode Error Prone only runs the Refaster rule, so the build succeeds despite `ShortSet` and `ReportDate`.

The classes compiled in this run come from the sources as they were before the rewrite. Build again
to compile the rewritten code, and restore the original with git:

```sh
mvn clean verify
git checkout demo/src/main/java/org/jugistanbul/CustomerValidator.java
```

## Writing your own check

Each check in [`custom-checks`](custom-checks/src/main/java/org/jugistanbul/checks) is a short,
self-contained example:

- A `BugChecker` subclass declares the AST nodes it inspects by implementing `*TreeMatcher`
  interfaces, and returns a `Description`, optionally with a `SuggestedFix`.
- `@BugPattern` sets the check's summary and severity. A check can be turned off or changed with
  `-Xep:<CheckName>:OFF|WARN|ERROR`, or suppressed locally with `@SuppressWarnings("<CheckName>")`.
- `@AutoService(BugChecker.class)` registers the check, so adding the jar to the annotation processor
  path is enough to load it.
- Tests use `CompilationTestHelper`, with `// BUG: Diagnostic contains:` comments marking the
  expected findings, and `BugCheckerRefactoringTestHelper` for suggested fixes.

The tests compile code with `javac` in the test JVM, so the same `--add-exports` / `--add-opens`
flags are passed to Surefire in [`custom-checks/pom.xml`](custom-checks/pom.xml).

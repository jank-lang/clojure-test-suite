# Running the Test Suite with ClojureCLR

## Installing ClojureCLR

- [dotnet](https://dotnet.microsoft.com/en-us/download)
- `ClojureCLR`: `dotnet tool install --global Clojure.Main --version 1.12.2`
- `cljr`: `dotnet tool install --global Clojure.Cljr --version 0.1.0-alpha6`

## Running the Tests

```bash
cljr -X:test
```

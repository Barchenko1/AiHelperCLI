# AiHelperCLI

Local program for screen and voice making for AI helper

# Build

` ./gradlew clean build `

# Run
## MAC
```
VERIFICATION_CODE="your code" java -jar build/libs/AiHelperCLI.jar

```
### Run a background mode
```
VERIFICATION_CODE=320270 nohup java -jar build/libs/AiHelperCLI.jar \
> aihelper.out 2>&1 & echo $! > aihelper.pid
```

### Check logs
```
tail -f aihelper.out
```
### Or
```
lsof -i :8080 
kill $(cat aihelper.pid)
```

### Stop background mode
```
kill $(cat aihelper.pid)
sleep 2
kill -9 $(cat aihelper.pid) 2>/dev/null || true   # force if still alive
rm -f aihelper.pid
```

## Windows
```
$env:VERIFICATION_CODE = "your code"
java -jar build\libs\AiHelperCLI.jar
```
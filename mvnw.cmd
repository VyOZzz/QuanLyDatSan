@echo off
setlocal ENABLEDELAYEDEXPANSION

REM === Lightweight Maven Wrapper (simplified, no PowerShell variable gymnastics) ===
REM Fix for previous failure where ^$u / ^$o were not parsed in PowerShell.

if "%MAVEN_VERSION%"=="" set MAVEN_VERSION=3.9.11
set DIST_FILE=apache-maven-%MAVEN_VERSION%-bin.zip
set DIST_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/%DIST_FILE%
set TARGET_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%\plain
set MAVEN_HOME=%TARGET_DIR%\apache-maven-%MAVEN_VERSION%
set ZIP_PATH=%TARGET_DIR%\dist.zip

if exist "%MAVEN_HOME%\bin\mvn.cmd" goto run

echo [Wrapper] Maven %MAVEN_VERSION% not found. Downloading...
if not exist "%TARGET_DIR%" mkdir "%TARGET_DIR%" >NUL 2>&1

REM Try curl first (Windows 10+ usually has it)
where curl >NUL 2>&1
if %ERRORLEVEL%==0 (
  echo [Wrapper] Using curl to download Maven...
  curl -L -o "%ZIP_PATH%" "%DIST_URL%" || goto fail
) else (
  where powershell >NUL 2>&1
  if %ERRORLEVEL% NEQ 0 (
    echo [Wrapper] Neither curl nor PowerShell are available. Please install Maven manually.
    goto fail
  )
  echo [Wrapper] Using PowerShell Invoke-WebRequest to download Maven...
  powershell -NoLogo -NoProfile -Command "Invoke-WebRequest -UseBasicParsing -Uri '%DIST_URL%' -OutFile '%ZIP_PATH%'" || goto fail
)

REM Extract (PowerShell Expand-Archive is simplest & present on Win10+)
echo [Wrapper] Extracting...
where powershell >NUL 2>&1
if %ERRORLEVEL% NEQ 0 (
  echo [Wrapper] PowerShell required to extract archive. Please extract manually: %ZIP_PATH%
  goto fail
)
powershell -NoLogo -NoProfile -Command "Expand-Archive -Path '%ZIP_PATH%' -DestinationPath '%TARGET_DIR%' -Force" || goto fail

del /f /q "%ZIP_PATH%" >NUL 2>&1
echo [Wrapper] Download + extract OK.

:run
"%MAVEN_HOME%\bin\mvn.cmd" %*
endlocal
exit /b %ERRORLEVEL%

:fail
echo [Wrapper] Failed to setup Maven wrapper.
endlocal
exit /b 1

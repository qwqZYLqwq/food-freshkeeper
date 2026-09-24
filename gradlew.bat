@echo off
setlocal
set DIR=%~dp0
set GRADLE_WRAPPER_JAR=%DIR%gradle\wrapper\gradle-wrapper.jar
set JAVA_EXE=java
"%JAVA_EXE%" -classpath "%GRADLE_WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*

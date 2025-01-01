### Docker Build instructions

Compile with Gradle

```bash
cd standalone/loader/build/libs

cp MoonRise-*.jar moonrise-standalone.jar
docker build . -t moonrise -f ../../../docker/Dockerfile
```
# External / Common JARs Directory

Place custom, third-party, or unmanaged external `.jar` files in this directory (e.g. CBS connectors, legacy banking drivers, hardware security modules).

### How to reference external JARs in `pom.xml`:

```xml
<dependency>
    <groupId>com.bank.custom</groupId>
    <artifactId>custom-cbs-connector</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/jars/custom-cbs-connector-1.0.0.jar</systemPath>
</dependency>
```

Alternatively, install into local maven cache:
```bash
mvn install:install-file \
   -Dfile=jars/your-library.jar \
   -DgroupId=com.bank.common \
   -DartifactId=common-lib \
   -Dversion=1.0.0 \
   -Dpackaging=jar
```

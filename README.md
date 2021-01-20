# gradle-desugar-plugin
Gradle plugin for running Desugar

## Usage

```
buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }
    dependencies {
		classpath "com.viridiansoftware:gradle-desugar-task:1.1.0"
	}
}

task desugarJdk(type: com.viridiansoftware.desugar.DesugarTask, dependsOn: jarJdk) {
    inputJar = file('build/libs/jdk.jar')
    outputJar = file('build/libs/jdk-desugar.jar')
    bootstrapClasspath = files('path/to/jdk.jar')
    classpath = files('path/to/classpath.jar')
    minSdkVersion 19
}
```
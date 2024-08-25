# moshi-ext-plugin
[moshi](https://github.com/square/moshi)  扩展插件可以解决data类不能混淆的问题
# How to use 

### step 1
```
 defaultConfig {
    ksp { arg("moshi.generateProguardRules", "false") }
 }
```
### step 2
```
dependencies {
    ksp("io.github.cloak-box.ksp:moshi-codeGen:1.0.0.1")
}
```
### step 3
```
repositories {
    mavenCentral()
}
```
### step 4
```
buildscript {
  dependencies {
    classpath("io.github.cloak-box.plugin:proguardConfig:1.0.0.1")
  }
}
```
### step 5
```
R8proguardConfig{
  replaceConfig{
    this["/transformed/rules/lib/META-INF/proguard/moshi.pro"]=File("${project.projectDir.absolutePath}/moshi.pro").absolutePath
  }
}
```
### step 6
[moshi.pro](moshi.pro)
```
-dontwarn javax.annotation.**
-keep @com.squareup.moshi.JsonQualifier @interface *
-keepclassmembers @com.squareup.moshi.JsonClass class * extends java.lang.Enum {
    <fields>;
    **[] values();
}
-keepclassmembers class com.squareup.moshi.internal.Util {
    private static java.lang.String getKotlinMetadataClassName();
}

```
# License
[GPL3](LICENSE) 

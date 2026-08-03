-keep class com.modark.reset.** { *; }
-dontobfuscate
-keepattributes *
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

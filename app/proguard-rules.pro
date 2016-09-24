# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/drool/android-sdk-macosx/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}


# Preserve some attributes that may be required for reflection.
-keepattributes *Annotation*,Signature,InnerClasses

# Preserve line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute ''

# Preserve constructors and setters of View implementations.
-keepclassmembers !abstract class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Preserve the CREATOR field of Parcelable implementations.
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# Preserve annotated Javascript interface methods.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

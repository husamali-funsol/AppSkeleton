# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


#Uncomment for Glide library performance if you are using Glide Library in your project

#-keep public class * implements com.bumptech.glide.module.GlideModule
#-keep class * extends com.bumptech.glide.module.AppGlideModule {
#    <init>(...);
#}
#-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
#    **[] $VALUES;
#    public *;
#}
#-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
#    *** rewind();
#}
## Uncomment for DexGuard only
##-keepresourcexmlelements manifest/application/meta-data@value=GlideModule
#
#https://github.com/bumptech/glide/blob/master/library/proguard-rules.txt

#For Navigation Component Library
-keepattributes RuntimeVisibleAnnotations
-keep class * extends androidx.navigation.Navigator

#For Google Ads Library
#-keep class com.google.ads.** # Don't proguard AdMob classes
#-dontwarn com.google.ads.** # Temporary workaround for v6.2.1. It gives a warning that you can ignore

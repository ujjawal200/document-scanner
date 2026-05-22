# Keep OpenCV
-keep class org.opencv.** { *; }
-dontwarn org.opencv.**

# Keep iText
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**
-dontwarn org.bouncycastle.**
-dontwarn org.slf4j.**

# Keep ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Keep app models
-keep class com.ujjawal.docscanner.model.** { *; }

# Keep ViewBinding
-keep class com.ujjawal.docscanner.databinding.** { *; }

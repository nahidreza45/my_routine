# Keep kotlinx.serialization classes
-keepattributes *Annotation*, InnerClasses, Signature
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.nahidreza.routine.model.**$$serializer { *; }
-keepclassmembers class com.nahidreza.routine.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.nahidreza.routine.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
}

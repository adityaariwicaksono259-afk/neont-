# Simpan Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Simpan Talsec
-keep class com.aheaditec.talsec.** { *; }

# Simpan Model
-keep class app.neont.api.MessageModel { *; }
-keep class app.neont.api.UserModel { *; }

# Simpan Activity (biar Manifest nggak error)
-keep class app.neont.api.** extends android.app.Activity { *; }

# Simpan native method
-keepclasseswithmembernames class * { native <methods>; }

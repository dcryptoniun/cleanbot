# Project-specific R8 rules go here.

# --- Conscrypt Public TLS 1.3 Provider Rules ---
-keep class org.conscrypt.** { *; }
-dontwarn org.conscrypt.**

# --- BouncyCastle Crypto Rules ---
-keep class org.bouncycastle.jcajce.provider.asymmetric.RSA$Mappings { <init>(); }
-keep class org.bouncycastle.jcajce.provider.asymmetric.X509$Mappings { <init>(); }
-keep class org.bouncycastle.jcajce.provider.asymmetric.rsa.DigestSignatureSpi$SHA256 { <init>(); }
-keep class org.bouncycastle.jcajce.provider.asymmetric.rsa.KeyFactorySpi { <init>(); }
-keep class org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory { <init>(); }
-dontwarn org.bouncycastle.**

# --- Spake2 Crypto Rules ---
-keep class com.flyfish233.crypto.spake2.** { *; }
-dontwarn com.flyfish233.crypto.spake2.**

# --- Relocate inlined ADB protocol classes into cLeanBot namespace ---
-repackageclasses 'com.gerstudio.cleanbot.internal'
-allowaccessmodification

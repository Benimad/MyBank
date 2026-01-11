package com.example.mybank.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.example.mybank.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val InterFont = GoogleFont("Inter")

val InterFontFamily = FontFamily(
    Font(googleFont = InterFont, fontProvider = provider, weight = androidx.compose.ui.text.font.FontWeight.Thin),
    Font(googleFont = InterFont, fontProvider = provider, weight = androidx.compose.ui.text.font.FontWeight.ExtraLight),
    Font(googleFont = InterFont, fontProvider = provider, weight = androidx.compose.ui.text.font.FontWeight.Light),
    Font(googleFont = InterFont, fontProvider = provider, weight = androidx.compose.ui.text.font.FontWeight.Normal),
    Font(googleFont = InterFont, fontProvider = provider, weight = androidx.compose.ui.text.font.FontWeight.Medium),
    Font(googleFont = InterFont, fontProvider = provider, weight = androidx.compose.ui.text.font.FontWeight.SemiBold),
    Font(googleFont = InterFont, fontProvider = provider, weight = androidx.compose.ui.text.font.FontWeight.Bold),
    Font(googleFont = InterFont, fontProvider = provider, weight = androidx.compose.ui.text.font.FontWeight.ExtraBold),
    Font(googleFont = InterFont, fontProvider = provider, weight = androidx.compose.ui.text.font.FontWeight.Black)
)

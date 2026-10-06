package com.worthit.gastospersonales.core.ui

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

// Locale-aware display ("Sep 30, 2026" / "30 sept 2026"). Storage stays a plain LocalDate.
fun LocalDate.toDisplayText(locale: Locale): String =
    format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))

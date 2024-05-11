package com.pokhuimand.photoeditor.models

import android.net.Uri
import java.time.LocalDateTime

data class Photo(val id: String, val uri: Uri, val creationDate: LocalDateTime)

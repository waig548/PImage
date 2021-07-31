package waig548.PImage

import waig548.PImage.PayloadProperty.Type
import java.io.File

data class Reference(
    val path: String,
    val type: Type,
    val file: File
)
{
    constructor(path: String, type: Type): this(path, type, File(path))
    constructor(path: String, type: String): this(path, Type.of(type))
}

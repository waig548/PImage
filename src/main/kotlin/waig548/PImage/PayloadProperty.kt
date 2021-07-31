package waig548.PImage

import java.io.File

data class PayloadProperty(
    //val bytes: ByteArray?,
    val size: Int,
    val type: Type,
    val path: String,
    val file: File
)
{
    constructor(bytes: ByteArray, reference: Reference): this(bytes.size, reference.type, reference.path, reference.file)
    constructor(size: Int, type: Type, path: String): this(size, type, path, File(path))

    enum class Type
    {
        TEXT,
        BINARY;

        companion object
        {
            fun of(key: String): Type =values().first {key.equals(it.name, true)}
        }
    }
}


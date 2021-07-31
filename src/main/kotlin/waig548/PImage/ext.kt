package waig548.PImage

fun Int.toByteArray(): ByteArray =
    byteArrayOf(ushr(24).toByte(), ushr(16).toByte(), ushr(8).toByte(), toByte())

@ExperimentalStdlibApi
fun ByteArray.toInt(): Int
{
    if(size!=4)
        throw IllegalStateException("Invalid array size")
    return  "%02x".format(this[0]).toInt(16).rotateLeft(24) or
            "%02x".format(this[1]).toInt(16).rotateLeft(16) or
            "%02x".format(this[2]).toInt(16).rotateLeft(8)  or
            "%02x".format(this[3]).toInt(16)
}

fun Byte.slice(): List<Int> = "%02x".format(this).map {it.toString().toInt(16)}

fun Int.slice(): List<Int> = "%02x".format(this).map {it.toString().toInt(16)}

fun packToByte(msb: Int, lsb: Int): Byte =(msb shl 4 or lsb).toByte()

fun packToInt(msb: Int, lsb: Int): Int =msb shl 4 or lsb

val SectorSize = 0x10
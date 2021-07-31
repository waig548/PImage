package waig548.PImage

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import waig548.PImage.PayloadProperty.Type

@ExperimentalStdlibApi
object Decompressor
{
    private fun readByte(img: BufferedImage, x: Int, y: Int): Byte
    {
        val px=Color(img.getRGB(x, y))
        return packToByte(px.green.slice().last(), px.blue.slice().last())
    }

    private fun readByte(img: BufferedImage, pos: Int): Pair<Byte, Int>
    {
        return Pair(readByte(img, pos%img.width, pos/img.width), pos+1)
    }

    private fun readStream(img: BufferedImage, startPos: Int, size: Int): ByteArray
    {
        var pos=startPos
        val bytes=ByteArray(size)
        for(i in bytes.indices)
            readByte(img, pos).run {
                bytes[i]=first
                pos=second
            }
        return bytes
    }

    private fun writePayloadToFile(bytes:ByteArray, property: PayloadProperty)
    {
        property.file.parentFile.mkdirs()
        when(property.type)
        {
            Type.TEXT -> property.file.writeText(bytes.toString(Charsets.UTF_8))
            Type.BINARY -> property.file.writeBytes(bytes)
        }
    }

    private fun readOffsetTable(img: BufferedImage, outputPath: String): Map<Int, PayloadProperty>
    {
        val offsetTable = mutableMapOf<Int, PayloadProperty>()
        var idx = 0
        while(true)
        {
            val sector = readStream(img, idx* SectorSize, SectorSize)
            if(sector.filterNot {it==0.toByte()}.isEmpty())
                break
            val (id, pSize, pExt)=Triple(
                sector.sliceArray(0..3).toInt(),
                sector.sliceArray(4..7).toInt(),
                sector.sliceArray(8..15).toString(Charsets.UTF_8).replace("\u0000", "")
            )
            val type=Type.values()[id ushr 31]
            val pPos=id shl 1 shr 1
            offsetTable[pPos]= PayloadProperty(pSize, type, "$outputPath${File.separator}payload$idx.$pExt")
            idx++
        }
        return offsetTable
    }

    private fun readPayloads(img: BufferedImage, offsetTable: Map<Int, PayloadProperty>): Map<PayloadProperty, ByteArray>
    {
        val payloads = mutableMapOf<PayloadProperty, ByteArray>()
        for((p, prop) in offsetTable)
            payloads[prop] = readStream(img, p, prop.size)
        return payloads
    }

    fun decompress(image: File, outputPath: String)
    {
        val img=ImageIO.read(image)
        for((prop, bytes) in readPayloads(img, readOffsetTable(img, outputPath)))
            writePayloadToFile(bytes, prop)
    }
}
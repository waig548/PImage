package waig548.PImage

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import waig548.PImage.PayloadProperty.Type

object Compressor
{
    private val offsetTable = mutableMapOf<Int, PayloadProperty>()
    private var ptr = 0
    private var imgSize: Int = 0
    private var reservedSize: Int = 0

    private fun writeByte(img: BufferedImage, pos: Int, byte: Byte): Int
    {
        writeByte(img, pos % img.width, pos / img.width, byte)
        return pos + 1
    }

    private fun writeByte(img: BufferedImage, x: Int, y: Int, byte: Byte)
    {
        val oPx = Color(img.getRGB(x, y))
        val (msb, lsb) = byte.slice()
        val nPx = Color(oPx.red, packToInt(oPx.green.slice()[0], msb), packToInt(oPx.blue.slice()[0], lsb), oPx.alpha)
        img.setRGB(x, y, nPx.rgb)
    }

    private fun writeStream(img: BufferedImage, startPos: Int, bytes: ByteArray): Int
    {
        var pos = startPos
        for(byte in bytes)
            pos = writeByte(img, pos, byte)
        return pos
    }

    private fun preProcess(img: BufferedImage, refs: List<Reference>)
    {
        imgSize = img.width * img.width
        reservedSize = SectorSize * (refs.size + 1)
        ptr = reservedSize
    }

    private fun writePayloads(img: BufferedImage, refs: List<Reference>)
    {
        for((index, ref) in refs.withIndex())
        {
            ptr = if(ptr % 4 == 0) ptr else (ptr / 4 + 1) * 4
            val payload = when(ref.type)
            {
                Type.TEXT -> ref.file.readText().toByteArray()
                Type.BINARY -> ref.file.readBytes()
            }
            offsetTable[ptr] = PayloadProperty(payload, ref)
            if(ptr + payload.size >= imgSize)
                throw IllegalStateException("Not enough space for payload index $index with size ${payload.size} @ pos $ptr (${ptr % img.width}, ${ptr / img.height})")
            ptr = writeStream(img, ptr, payload)
        }
        ptr = 0
    }

    private fun writeOffsetTable(img: BufferedImage)
    {
        for((p, payload) in offsetTable)
        {
            val id = when(payload.type)
            {
                Type.TEXT -> 0; Type.BINARY -> 1
            } shl 31 or p
            ptr = writeStream(img, ptr, id.toByteArray())
            ptr = writeStream(img, ptr, payload.size.toByteArray())
            ptr = writeStream(
                img,
                ptr,
                payload.file.extension.toByteArray().apply {if(this.size > 16 - ptr % 16) sliceArray(0..15 - ptr % 16)})
            while(ptr % 16 != 0)
                ptr = writeByte(img, ptr, 0.toByte())
        }
        ptr = writeStream(img, ptr, ByteArray(16))
    }

    fun compress(image: File, payloads: List<Reference>, output: File)
    {
        val img = ImageIO.read(image)
        preProcess(img, payloads)
        writePayloads(img, payloads)
        writeOffsetTable(img)
        ImageIO.write(img, "PNG", output)
    }
}
import org.junit.Test
import waig548.PImage.Compressor
import waig548.PImage.Decompressor
import waig548.PImage.Reference
import java.io.File

class Tests
{
    @Test
    fun `Compress Test Group 1`()
    {
        val imgFile = File("05193m.png")
        val outFile = File("out.png")
        val payloads = listOf(
            Reference("payload.ps1", "text"),
            Reference("some_text.txt", "text"),
            Reference("some_bin", "binary")
        )
        Compressor.compress(imgFile, payloads, outFile)
    }

    @ExperimentalStdlibApi
    @Test
    fun `Decompress Test Group 1`()
    {
        val imgFile = File("out.png")
        val outDir = "output"
        Decompressor.decompress(imgFile, outDir)
    }

    @ExperimentalStdlibApi
    @Test
    fun `Check Test Group 1`()
    {
        `Compress Test Group 1`()
        `Decompress Test Group 1`()
        check(File("payload.ps1").readText()==File("output\\payload0.ps1").readText())
        check(File("some_text.txt").readText()==File("output\\payload1.txt").readText())
        check(File("some_bin").readBytes().contentEquals(File("output\\payload2").readBytes()))
    }
}
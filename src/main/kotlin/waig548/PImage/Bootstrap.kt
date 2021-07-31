package waig548.PImage

import java.io.File
import kotlin.system.exitProcess

object Bootstrap
{
    val Syntax = """
        
        Usage:  java -jar PImage.jar {-c | -e} -i input_file [-p [payload_option] payload_file]... 
                                               -o {output_file_name.png | output_dir}
        Options: 
            -c              Compress provided payloads into the input_file and output a PNG file 
                            of the input image with payloads inside
            -e              Extract payloads from input_file into output_dir
            -i input_file   Path to the input file, should be an image
            -o output       Path to the output PNG file or output directory, depending on the operation mode.
            -p              Specify a payload to include
            -h              Display help
            
        Payload Syntax:     -p {-t | -b} payload_file
        
        Payload Options:    
            -t              Indicate the payload is a text file
            -b              Indicate the payload is a binary file
        """.trimIndent()

    fun buildOptions(args: Array<String>): List<Option>
    {
        val opts = mutableListOf<Option>()
        var idx = 0
        while(idx < args.size)
        {
            val type = requireNotNull(Option.Type.of(args[idx]))
            val arg = mutableListOf<String>()
            if(type.argNum != 0)
                for(i in 0 until type.argNum)
                    if(Option.Type.of(args[idx + 1 + i]) ==null)
                        arg.add(args[idx + 1 + i])
                    else
                        throw IllegalArgumentException("Invalid argument @ index ${idx +1 +i}")
            opts.add(Option(type, arg))
            idx += 1 + type.argNum
        }
        return opts
    }

    fun checkOptions(opts: List<Option>)
    {
        if(!(opts.map {it.type}.contains(Option.Type.Compress) xor opts.map {it.type}.contains(Option.Type.Decompress)))
            throw IllegalArgumentException("Either -c or -e option is required to execute.")
        when {
            opts.filter {it.type== Option.Type.Input}.size!=1 -> throw IllegalArgumentException("Only one input file is required to execute.")
            opts.filter {it.type== Option.Type.Output}.size!=1 -> throw IllegalArgumentException("Only one output path is required to execute.")
        }
        opts.filter {it.type== Option.Type.Payload}.forEachIndexed {index, option ->
            if(option.args[0]!="-b" && option.args[0]!="-t")
                throw IllegalArgumentException("Invalid payload type @ payload $index.")
        }

    }

    fun processOptions(opts: List<Option>): Pair<State, Triple<File, String, List<Reference>>>
    {
        val inputFile = File(opts.first{it.type== Option.Type.Input}.args[0])
        val outputPath = opts.first {it.type== Option.Type.Output}.args[0]
        val payloads = opts.filter {it.type== Option.Type.Payload}.map {Reference(it.args[1], if(it.args[0]=="-t") PayloadProperty.Type.TEXT else PayloadProperty.Type.BINARY)}
        return Pair(
            if (opts.map {it.type}.contains(Option.Type.Compress)) State.Compress else State.Decompress,
            Triple(inputFile, outputPath, payloads)
        )
    }

    @ExperimentalStdlibApi
    @JvmStatic
    fun main(args: Array<String>)
    {
        if (args.isEmpty() || args.contains("-h"))
        {
            println(Syntax)
            exitProcess(0)
        }
        val opts = buildOptions(args)
        checkOptions(opts)
        val (state, cmd) = processOptions(opts)
        val (inF, oP, plds) = cmd
        when (state)
        {
            State.Compress -> Compressor.compress(inF, plds, File(oP))
            State.Decompress -> Decompressor.decompress(inF, oP)
        }
    }

    enum class State
    {
        Compress,
        Decompress
    }

    class Option(
        val type: Type,
        val args: List<String>
    )
    {
        enum class Type(val key: String, val argNum: Int)
        {
            Compress("-c", 0),
            Decompress("-e", 0),

            Input("-i", 1),
            Output("-o", 1),
            Payload("-p", 2),

            Help("-h", 0);

            companion object
            {
                fun of(key: String) = values().firstOrNull {key == it.key}
            }
        }
    }
}
import dev.mee42.kargs.*
import java.io.File

class Args: Kargs() {
    val help by flag('h')
    val file by arg<File>('f')
}

fun main(args: Array<String>) {
    val args = Kargs.parse(args, ::Args)
    println(args.help)
    println(args.file)
}
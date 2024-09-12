package saj;

import kotlin.system.*;

// handles main

class CommandException(msg: String?) : Exception(msg);

fun main(args: Array<String>) {
	val cmd = if(args.size > 0) args[0] else "help";
	val subArgs = args.slice(1..<args.size);
	try {
		when(cmd) {
			"help" -> {
				println("Commands:");
				println("\thelp                                Prints this help info.");
				println("\taudio <word> <file> [scaling = 10]  Takes a word and outputs its waveform to <file>. If <word> is -, it reads from stdin.");
				println("\trotate <word>                       Takes a word, rotates it (applying rotation mutation) and outputs it to stdout. If <word> is -, it reads from stdin.");
				println("\tword [syllables = 2]                Generates a word with [syllables] syllables.");
			}
			"word" -> {
				generateWord(subArgs);
			}
			"audio" -> {
				generateAudio(subArgs);
			}
			"rotate" -> {
				try {
					if(subArgs.size != 1)
						throw CommandException("Usage: saj rotate <word>");
					val wordStr = if(subArgs[0] == "-") generateSequence(::readLine).joinToString("\n") else subArgs[0];
					val word = Word.parse(wordStr);
					println(word.rotate());
				} catch(e: IllegalArgumentException) {
					throw CommandException(e.message);
				}
			}
			else -> {
				println("Invalid command: $cmd");
				exitProcess(1);
			}
		}
	} catch(e: CommandException) {
		println(e.message);
		exitProcess(1);
	}
}

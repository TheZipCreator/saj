package saj;

// takes a single argument (syllable count) and generates a word. If syllable count is not specified, it defaults to 2.

fun generateWord(args: List<String>) {
	var syllables = 2;
	if(args.size != 0) {
		try {
			syllables = args[0].toInt();	
		} catch(e: NumberFormatException) {
			println("Invalid number: ${args[0]}.");
			return;
		}
	}
	val word = Word.syllable();
	var sylCount = 1;
	while(sylCount < syllables) {
		if(word.adjoin(Word.syllable()))
			sylCount++;
	}
	println(word.toString());
}

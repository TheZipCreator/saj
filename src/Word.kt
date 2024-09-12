package saj;

// contains logic for phonemes and words

import kotlin.random.*;

// chooses a random element from a list
fun <T> List<T>.choose() = this[Random.nextInt(0, size)];

// A point
data class Point(val x: Int, val y: Int) {
	operator fun unaryMinus() = Point(-x, -y);
	operator fun plus(p: Point) = Point(x+p.x, y+p.y);
	operator fun minus(p: Point) = Point(x-p.x, y-p.y);
}

// A 2D integer range
data class IntRange2(val minX: Int, val maxX: Int, val minY: Int, val maxY: Int) : Iterable<Point> {
	// Goes left-to-right, top-to-bottom
	override operator fun iterator() = object : Iterator<Point> {
		var x = minX;
		var y = minY;

		override fun hasNext() = y <= maxY;
		override fun next(): Point {
			val p = Point(x, y);
			x++;
			if(x > maxX) {
				y++;
				x = 0;
			}
			return p;
		}
	}
	val width
		get() = maxX-minX+1;
	val height
		get() = maxY-minY+1;
}

// type of a phoneme
enum class PhonemeType {
	CONSONANT,
	VOWEL
}

// A sound
data class Phoneme(val lateral: String, val vertical: String) {
	
	// only need to check one sense a sound can either be only vowels or only consonants
	val type: PhonemeType
		get() = if(vowels.contains(lateral)) PhonemeType.VOWEL else PhonemeType.CONSONANT;
	
	// standard notation
	override fun toString() = "$lateral/$vertical";

	// rotates a phoneme
	fun rotate(): Phoneme {
		val r = Phoneme(vertical, lateral);
		if(r in rotationMutation)
			return rotationMutation[r]!!;
		return r;
	}

	companion object {
		// consonant components
		val consonants = listOf("p", "p'", "t", "t'", "k", "k'", "m", "f", "s", "ʂ", "h", "j", "l");
		// vowel components
		val vowels = listOf("i", "a", "u", "ə");
		// what consonants (lateral) are compatible with what other consonants (vertical)
		val compatible = mapOf(
			"p" to listOf("p", "t", "k", "m", "f", "l"),
			"p'" to listOf("p'", "t'", "k'", "m", "l"),
			"t" to listOf("p", "t", "k", "m", "f", "l"),
			"t'" to listOf("p'", "t'", "k'", "m", "l"),
			"k" to listOf("p", "t", "k", "m", "f", "l"),
			"k'" to listOf("p'", "t'", "k'", "m", "l"),
			"m" to listOf("p", "p'", "t", "t'", "k", "k'", "m", "f", "l"),
			"f" to listOf("p", "t", "k", "m", "f", "l"),
			"s" to listOf("p", "p'", "t", "t'", "k", "k'", "m", "f", "s", "l"),
			"ʂ" to listOf("p", "p'", "t", "t'", "k", "k'", "m", "ʂ", "l"),
			"h" to listOf("h"),
			"j" to listOf("j"),
			"l" to listOf("l")
		);
		// rotation mutation
		val rotationMutation = mapOf(
			Phoneme("p", "s") to Phoneme("p", "f"),
			Phoneme("p'", "s") to Phoneme("p'", "p'"),
			Phoneme("t", "s") to Phoneme("t", "f"),
			Phoneme("t'", "s") to Phoneme("t'", "t'"),
			Phoneme("k", "s") to Phoneme("k", "f"),
			Phoneme("k'", "s") to Phoneme("k'", "k'"),
			Phoneme("m", "s") to Phoneme("m", "f"),
			Phoneme("f", "s") to Phoneme("f", "f"),
			Phoneme("p", "ʂ") to Phoneme("p", "l"),
			Phoneme("p'", "ʂ") to Phoneme("p'", "l"),
			Phoneme("t", "ʂ") to Phoneme("t", "l"),
			Phoneme("t'", "ʂ") to Phoneme("t'", "l"),
			Phoneme("k", "ʂ") to Phoneme("k", "l"),
			Phoneme("k'", "ʂ") to Phoneme("k'", "l"),
			Phoneme("m", "ʂ") to Phoneme("m", "l"),
			Phoneme("f", "ʂ") to Phoneme("f", "l"),
			Phoneme("l", "p") to Phoneme("f", "p"),
			Phoneme("l", "p'") to Phoneme("p'", "p'"),
			Phoneme("l", "t") to Phoneme("f", "t"),
			Phoneme("l", "t'") to Phoneme("t'", "t'"),
			Phoneme("l", "k") to Phoneme("f", "k"),
			Phoneme("l", "k'") to Phoneme("k'", "k'"),
			Phoneme("l", "m") to Phoneme("f", "m"),
			Phoneme("l", "f") to Phoneme("f", "f"),
			Phoneme("l", "s") to Phoneme("f", "s"),
		);
		// the placeholder phoneme
		val placeholder = Phoneme("ə", "ə");

		// generates a vowel
		fun vowel() = Phoneme(vowels.choose(), vowels.choose());
		// generates a consonant
		fun consonant(): Phoneme {
			val c = consonants.choose();
			return Phoneme(c, compatible[c]!!.choose());
		}
		// tests if a phoneme is valid
		fun isValid(lateral: String, vertical: String): Boolean {
			if(vowels.contains(lateral) && vowels.contains(vertical))
				return true;
			if(!consonants.contains(lateral) || !consonants.contains(vertical))
				return false;
			if(compatible[lateral]!!.contains(vertical))
				return true;
			return false;
		}
		// parses a phoneme
		fun parse(str: String): Phoneme {
			val split = str.replace("’", "'").split("/");
			if(split.size != 2)
				throw IllegalArgumentException("Phoneme '$str' must have a single slash.");
			if(!isValid(split[0], split[1]))
				throw IllegalArgumentException("Phoneme '$str' is invalid.");
			return Phoneme(split[0], split[1]);
		}
	}
}


// A word or syllable in Sajk'a
class Word(val phonemes: MutableMap<Point, Phoneme>) {	
	// converts to strings, separating phonemes by tabs
	override fun toString() = buildString {
		val range = range();
		for(y in range.minY..range.maxY) {
			for(x in range.minX..range.maxX) {
				val phoneme = phonemes[Point(x, y)];
				if(phoneme != null)
					append(phoneme.toString());
				if(x != range.maxX)
					append("\t");
			}
			append("\n");
		}
	};

	// Gets the range of the elements of this word
	fun range(): IntRange2 {
		val minX = phonemes.keys.minOf { it.x };
		val minY = phonemes.keys.minOf { it.y };
		val maxX = phonemes.keys.maxOf { it.x };
		val maxY = phonemes.keys.maxOf { it.y };
		return IntRange2(minX, maxX, minY, maxY);
	}

	// adjoins another word to this word in a random position. Returns whether the word could be adjoined without causing collisions
	fun adjoin(other: Word): Boolean {
		// possible placements found
		val placements = mutableListOf<Point>();

		// whether a given placement is possible
		fun canAdjoin(p: Point): Boolean {
			for(e in other.phonemes.entries) {
				val phoneme = phonemes[e.key+p];
				if(phoneme != null && phoneme != Phoneme.placeholder)
					return false;
			}
			return true;
		}
		
		// find placements
		for(e in phonemes.entries) {
			if(e.value != Phoneme.placeholder)
				continue; // can't adjoin here
			for(f in other.phonemes.entries) {
				val placement = e.key-f.key;
				if(canAdjoin(placement))
					placements.add(placement);
			}
		}
		if(placements.size == 0)
			return false;
		// chose placement at random and adjoin
		val placement = placements.choose();
		for(e in other.phonemes.entries) {
			phonemes[e.key+placement] = e.value;
		}
		return true;
	}
	// rotates this word, returning a new word
	fun rotate(): Word {
		val map = mutableMapOf<Point, Phoneme>();
		// point to reflect over
		val point = run {
			val range = range();
			Point(range.minX, range.maxY);
		}
		for(e in phonemes) {
			map[Point(point.y-e.key.y, point.x-e.key.x)] = e.value.rotate();
		}
		return Word(map);
	}

	companion object {
		// phonotactics of a syllable
		val phonotactics = mapOf<Point, PhonemeType>(
			Point(1, -2) to PhonemeType.CONSONANT,
			Point(0, -1) to PhonemeType.CONSONANT,
			Point(1, -1) to PhonemeType.CONSONANT,
			Point(2, -1) to PhonemeType.CONSONANT,
			Point(-1, 0) to PhonemeType.CONSONANT,
			Point(0, 0) to PhonemeType.VOWEL,
			Point(1, 0) to PhonemeType.CONSONANT,
			Point(-2, 1) to PhonemeType.CONSONANT,
			Point(-1, 1) to PhonemeType.CONSONANT,
			Point(0, 1) to PhonemeType.CONSONANT,
			Point(-2, 2) to PhonemeType.CONSONANT,
			Point(-1, 2) to PhonemeType.CONSONANT,
		);

		// generates a syllable
		fun syllable(): Word {
			var phonemes = mutableMapOf<Point, Phoneme>();
			// generate phonemes
			for(e in phonotactics.entries) {
				when(e.value) {
					PhonemeType.VOWEL -> phonemes[e.key] = Phoneme.vowel()
					PhonemeType.CONSONANT -> if(Random.nextBoolean()) phonemes[e.key] = Phoneme.consonant()
				}
			}
			// cull disconnected phonemes
			run {
				val visited = mutableSetOf<Point>();
				fun visit(p: Point) {
					if(p in visited)
						return;
					if(p !in phonemes)
						return;
					visited.add(p);
					visit(Point(p.x-1, p.y));
					visit(Point(p.x+1, p.y));
					visit(Point(p.x, p.y-1));
					visit(Point(p.x, p.y+1));
				}
				// relies on there being something at (0, 0)
				visit(Point(0, 0));
				val culled = mutableMapOf<Point, Phoneme>();
				for(p in visited)
					culled[p] = phonemes[p]!!;
				phonemes = culled;
			}
			if(phonemes.size == 1)
				return syllable(); // start over; syllables need at least 1 consonant
			// add ə/ə where word is unpronounceable
			run {
				val entries = phonemes.entries.toSet();
				fun vowelAt(p: Point) = p in phonemes && phonemes[p]!!.type == PhonemeType.VOWEL;
				for(e in entries) {
					if(e.value.type != PhonemeType.CONSONANT)
						continue;
					val p = e.key;
					val before = Point(p.x, p.y+1);
					val katopin = Point(p.x-1, p.y);
					val after = Point(p.x, p.y-1);
					val prin = Point(p.x+1, p.y);
					if(!vowelAt(before) && after !in phonemes)
						phonemes[after] = Phoneme.placeholder;
					if(!vowelAt(katopin) && prin !in phonemes)
						phonemes[prin] = Phoneme.placeholder;
				}
			}
			return Word(phonemes);
		}
		// parses a word
		fun parse(str: String): Word {
			val map = mutableMapOf<Point, Phoneme>();
			val split = str.lines().map { it.split("\t").map { it.trim { it == ' ' } } };
			for(i in 0..<split.size) {
				val line = split[i];
				for(j in 0..<line.size) {
					val pStr = line[j];
					if(pStr == "")
						continue;
					map[Point(j, i)] = Phoneme.parse(pStr);
				}
			}
			return Word(map);
		}
	}
}

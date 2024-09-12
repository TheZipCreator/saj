package saj;

import java.nio.file.*;
import java.nio.*;
import java.io.*;
import kotlin.system.*;
import javax.sound.sampled.*;
import java.awt.image.*;
import javax.imageio.*;

// generates audio images. Arguments are: string output-filename

val soundDir = Paths.get("./sounds");
val soundImageDir = Paths.get("./soundImages");
val baseImageSize = 44100;

fun generateAudio(args: List<String>) {
	if(args.size != 2 && args.size != 3)
		throw CommandException("Usage: saj audio <word> <file> [scaling = 10]");
	val scaling = if(args.size == 3) args[2].toIntOrNull() else 10;
	if(scaling == null)
		throw CommandException("Invalid value for [scaling].");
	createSoundImages(scaling);
	val wordStr = if(args[0] == "-") generateSequence(::readLine).joinToString("\n") else args[0];
	try {
		val imageSize = baseImageSize/scaling;
		val dir = soundImageDir.resolve("$scaling");
		val word = Word.parse(wordStr);
		val map = word.phonemes;
		val range = word.range();
		val img = BufferedImage(imageSize*(range.width), imageSize*(range.height), BufferedImage.TYPE_BYTE_GRAY);
		val raster = img.raster;
		raster.setPixels(0, 0, img.width, img.height, IntArray(img.width*img.height) { 128 });
		val phonemeImages = mutableMapOf<Phoneme, WritableRaster>();
		for(p in range) {
			val phone = map[p];
			if(phone == null)
				continue;
			val srcRaster = phonemeImages.getOrPut(phone) { ImageIO.read(dir.resolve("${phone.lateral}_${phone.vertical}.png").toFile()).raster };
			raster.setRect(imageSize*(p.x+range.minX), imageSize*(p.y+range.minY), srcRaster);
		}
		ImageIO.write(img, "png", File(args[1]));
	} catch(e: IllegalArgumentException) {
		throw CommandException(e.message);
	}
}

fun createSoundImages(scaling: Int) {
	val dir = soundImageDir.resolve("$scaling");
	if(!Files.exists(dir)) {
		println("Sound images do not exist. Creating...");
		Files.createDirectories(dir);
	} else {
		return;
	}
	if(!Files.isDirectory(soundDir)) {
		throw CommandException("Sounds directory does not exist!");
	}
	val audio = mutableMapOf<String, IntArray>();
	val imageSize = baseImageSize/scaling;
	// read audio bytes
	for(path in Files.list(soundDir)) {
		val name = path.getName(path.nameCount-1).toString().split(".")[0];
		AudioSystem.getAudioInputStream(path.toFile()).use { stream ->
			// check if it's in the format we expect it in
			val fmt = stream.format;
			if(fmt.isBigEndian() || fmt.encoding != AudioFormat.Encoding.PCM_SIGNED)
				throw CommandException("Audio file $path is not in the expected format.");
			// save to `audio`
			val arr = ByteArray(stream.available()) { 0 };
			stream.read(arr);
			val bb = ByteBuffer.wrap(arr);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			val intArr = IntArray(arr.size/2) { 0 };
			for(i in 0..<intArr.size)
				intArr[i] = bb.getShort().toInt();
			audio[name] = IntArray(intArr.size/scaling) { i -> 
				// .average() returns a double on an int array for some reason so I need to do this
				val slice = intArr.slice(i*scaling..<(i+1)*scaling);
				// slice.sum()/slice.size
				slice[0]
			};
		}
	}
	// create and output images
	val amplification = 0x4;
	// combine samples
	for(a in audio.keys) {
		val arrA = audio[a]!!;
		if(arrA.size != imageSize)
			throw CommandException("Invalid image size!");
		for(b in audio.keys) {
			val arrB = audio[b]!!;
			if(!Phoneme.isValid(a, b))
				continue;
			val img = BufferedImage(arrA.size, arrB.size, BufferedImage.TYPE_BYTE_GRAY);
			val raster = img.raster;
			for(i in 0..<arrA.size) {
				for(j in 0..<arrB.size) {
					val col = (((amplification*(arrA[i]+arrB[j]))/2 shr 8)+0x80).coerceIn(0, 0xFF);
					// val col = (amplification*(255.0*(arrA[i].toDouble()/512.0)*(arrB[j].toDouble()/512.0)).toInt()).coerceIn(0, 0xFF);
					raster.setSample(i, img.height-j-1, 0, col);
				}
			}
			val filename = "${a}_${b}.png";
			ImageIO.write(img, "png", dir.resolve(filename).toFile());
			println("Created $filename.");
		}
	}
}

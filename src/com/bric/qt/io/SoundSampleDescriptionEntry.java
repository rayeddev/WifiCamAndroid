/*
 * @(#)SoundSampleDescriptionEntry.java
 *
 * $Date: 2012-08-10 15:33:58 -0500 (Fri, 10 Aug 2012) $
 *
 * Copyright (c) 2012 by Jeremy Wood.
 * All rights reserved.
 *
 * The copyright of this software is owned by Jeremy Wood. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Jeremy Wood. For details see accompanying license terms.
 * 
 * This software is probably, but not necessarily, discussed here:
 * http://javagraphics.java.net/
 * 
 * That site should also contain the most recent official version
 * of this software.  (See the SVN repository for more details.)
 */
package com.bric.qt.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SoundSampleDescriptionEntry extends SampleDescriptionEntry {

	/** A 16-bit integer that holds the sample description version (currently 0 or 1) */
	int version = 0;
	
	/** A 16-bit integer that must be set to 0. */
	int revision = 0;
	
	/** A 32-bit integer that must be set to 0. */
	String vendor = "";
	
	/** A 16-bit integer that indicates the number of sound channels used by the
	 * sound sample. Set to 1 for monaural sounds, 2 for stereo sounds. Higher 
	 * numbers of channels are not supported.
	 **/
	int numberOfChannels;
	
	/** A 16-bit integer that specifies the number of bits in each uncompressed 
	 * sound sample. Allowable values are 8 or 16. Formats using more than 16 
	 * bits per sample set this field to 16 and use sound description version 1. 
	 */
	int bitsPerSample;
	
	/** A 16-bit integer that must be set to 0 for version 0 sound descriptions. 
	 * This may be set to -2 for some version 1 sound descriptions; see "Redefined 
	 * Sample Tables" (page 119). 
	 */
	int compressionID = 0;
	
	/** A 16-bit integer that must be set to 0. */
	int packetSize = 0;
	
	/** A 32-bit unsigned fixed-point number (16.16) that indicates the rate at which
	 *  the sound samples were obtained. The integer portion of this number should 
	 *  match the media's time scale. Many older version 0 files have values of 
	 *  22254.5454 or 11127.2727, but most files have integer values, such as 44100. 
	 *  Sample rates greater than 2^16 are not supported.
	 */
	float sampleRate;
	
	/** The number of uncompressed frames generated by a compressed frame (an 
	 * uncompressed frame is one sample from each channel). This is also the frame 
	 * duration, expressed in the media's timescale, where the timescale is equal 
	 * to the sample rate. For uncompressed formats, this field is always 1.
	 * 
	 * This field is only defined if the version field is non-zero.
	 */
	long samplesPerPacket;
	
	/** For uncompressed audio, the number of bytes in a sample for a single channel. 
	 * This replaces the older bitsPerSample field, which is set to 16. 
	 * 
	 * This value is calculated by dividing the frame size by the number of channels. 
	 * The same calculation is performed to calculate the value of this field for 
	 * compressed audio, but the result of the calculation is not generally meaningful 
	 * for compressed audio.
	 * 
	 * This field is only defined if the version field is non-zero.
	 */
	long bytesPerPacket;
	
	/** The number of bytes in a frame: for uncompressed audio, an uncompressed 
	 * frame; for compressed audio, a compressed frame. This can be calculated by 
	 * multiplying the bytes per packet field by the number of channels. 
	 * 
	 * This field is only defined if the version field is non-zero.
	 */
	long bytesPerFrame;
	
	/** The size of an uncompressed sample in bytes. This is set to 1 for 8-bit audio,
	 * 2 for all other cases, even if the sample size is greater than 2 bytes.
	 * 
	 * This field is only defined if the version field is non-zero.
	 */
	long bytesPerSample;
	
	/** 
	 * @param in
	 * @throws IOException
	 */
	public SoundSampleDescriptionEntry(InputStream in) throws IOException {
		super(in);
		version = Atom.read16Int(in);
		if(!(version==0 || version==1)) {
			System.err.println("SoundSampleDescriptionEntry: warning: unsupported version ("+version+")");
		}
		revision = Atom.read16Int(in);
		vendor = Atom.read32String(in);
		numberOfChannels = Atom.read16Int(in);
		bitsPerSample = Atom.read16Int(in);
		compressionID = Atom.read16Int(in);
		packetSize = Atom.read16Int(in);
		sampleRate = Atom.read16_16Float(in);
		
		if(version==1) {
			samplesPerPacket = Atom.read32Int(in);
			bytesPerPacket = Atom.read32Int(in);
			bytesPerFrame = Atom.read32Int(in);
			bytesPerSample = Atom.read32Int(in);
		}
	}

	/** Create a version 0 description entry.
	 * If bitsPerSample is 8, then the type is "raw ", otherwise the type is "twos".
	 */
	public SoundSampleDescriptionEntry(int dataReference,
			int numberOfChannels,
			int bitsPerSample,
			float sampleRate) {
		super( getType(bitsPerSample), dataReference);
		this.numberOfChannels = numberOfChannels;
		this.bitsPerSample = bitsPerSample;
		this.sampleRate = sampleRate;
		
		this.version = 1;
		this.samplesPerPacket = 1;
		this.bytesPerPacket = 1*bitsPerSample/8;
		this.bytesPerFrame = bytesPerPacket*numberOfChannels;
		this.bytesPerSample = bytesPerPacket;
	}
	
	private static String getType(int bitsPerSample) {
		if(bitsPerSample==8) return "raw ";
		if(bitsPerSample==16) return "twos";
		throw new IllegalArgumentException("bitsPerSample ("+bitsPerSample+") must be either 8 or 16");
	}

	@Override
	protected long getSize() {
		if(version==0) {
			return 16 + 20;
		}
		return 16 + 20 + 16;
	}
	
	@Override
	public String toString() {
		if(version==0) {
			return "SoundSampleDescriptionEntry[ type=\""+type+"\", "+
			"dataReference="+dataReference+", "+
			"version="+version+", "+
			"revision="+revision+", "+
			"vendor=\""+vendor+"\", "+
			"numberOfChannels="+numberOfChannels+", "+
			"bitsPerSample="+bitsPerSample+", "+
			"compressionID="+compressionID+", "+
			"packetSize="+packetSize+", "+
			"sampleRate="+sampleRate+", "+
			"samplesPerPacket=NA, "+
			"bytesPerPacket=NA, "+
			"bytesPerFrame=NA, "+
			"bytesPerSample=NA ]";
		} else {
			return "SoundSampleDescriptionEntry[ type=\""+type+"\", "+
			"dataReference="+dataReference+", "+
			"version="+version+", "+
			"revision="+revision+", "+
			"vendor=\""+vendor+"\", "+
			"numberOfChannels="+numberOfChannels+", "+
			"bitsPerSample="+bitsPerSample+", "+
			"compressionID="+compressionID+", "+
			"packetSize="+packetSize+", "+
			"sampleRate="+sampleRate+", "+
			"samplesPerPacket="+samplesPerPacket+", "+
			"bytesPerPacket="+bytesPerPacket+", "+
			"bytesPerFrame="+bytesPerFrame+", "+
			"bytesPerSample="+bytesPerSample+" ]";
		}
	}

	@Override
	protected void write(OutputStream out) throws IOException {
		Atom.write32Int(out, getSize());
		Atom.write32String(out, type);
		Atom.write48Int(out, 0);
		Atom.write16Int(out, dataReference);
		
		Atom.write16Int(out, version);
		Atom.write16Int(out, revision);
		Atom.write32String(out, vendor);
		Atom.write16Int(out, numberOfChannels);
		Atom.write16Int(out, bitsPerSample);
		Atom.write16Int(out, compressionID);
		Atom.write16Int(out, packetSize);
		Atom.write16_16Float(out, sampleRate);
		
		if(version==1) {
			Atom.write32Int(out, samplesPerPacket);
			Atom.write32Int(out, bytesPerPacket);
			Atom.write32Int(out, bytesPerFrame);
			Atom.write32Int(out, bytesPerSample);
		}
	}

}

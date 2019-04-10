package org.biojava3.genome.parsers.gff;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * http://www.bioperl.org/wiki/GTF Read and write FeatureLists as GFF/GTF
 * formatted files. <br>
 * <br>
 * The GFF moniker is applied to a variety of tab-delimited formats that mock
 * the notion of a standard. This class should parse most files bearing at least
 * a passing resemblance to any of the formats. You will, however, need to
 * research the semantics of the files you encounter. Generally, the format
 * consists of 9 tab-delimited fields: <br>
 * 
 * <pre>
 * seqname   source   featureType   start   end   score   strand   frame   attributes
 * </pre>
 * 
 * The 9th field consists of key-value pairs separated by semicolons, the first
 * of which JavaGene interprets as the group id (as used in GFF1). It is the
 * precise meaning of this 9th field that varies from week to week. The Feature
 * and FeatureList objects provide various utility methods to ease the task of
 * accessing and using the attributes. The proper interpretation of any
 * particular attribute, however, is left to you.
 *
 * @author Hanno Hinsch
 */
public class GFF3Reader {

	private static final Logger log = Logger.getLogger(GFF3Reader.class.getName());

	/**
	 * Read a file into a FeatureList. Each line of the file becomes one Feature
	 * object.
	 *
	 * @param filename The path to the GFF file.
	 * @return A FeatureList.
	 * @throws IOException Something went wrong -- check exception detail message.
	 */
	public static FeatureList read(String filename) throws IOException {
		log.info("Gff.read(): Reading " + filename);

		FeatureList features = new FeatureList();
		BufferedReader br = new BufferedReader(new FileReader(filename));

		String s;
		for (s = br.readLine(); null != s; s = br.readLine()) {
			s = s.trim();

			if (s.length() > 0) {
				if (s.charAt(0) == '#') {
					// ignore comment lines
					if (s.startsWith("##fasta"))
						break;
				} else {

					FeatureI f = parseLine(s);
					if (f != null) {
						features.add(f);
					}
				}
			}

		}

		br.close();
		return features;
	}

	/**
	 * create Feature from line of GFF file
	 */
	private static Feature parseLine(String s) {
		// FIXME update to use regex split on tabs
		// FIXME better errors on parse failures

		int start = 0;
		int end = 0;

		start = end;
		end = s.indexOf('\t', start);
		String seqname = s.substring(start, end).trim();

		start = end + 1;
		end = s.indexOf('\t', start);
		String source = s.substring(start, end).trim();

		start = end + 1;
		end = s.indexOf('\t', start);
		String type = s.substring(start, end);

		start = end + 1;
		end = s.indexOf('\t', start);
		String locStart = s.substring(start, end);

		start = end + 1;
		end = s.indexOf('\t', start);
		String locEnd = s.substring(start, end);

		Double score;
		start = end + 1;
		end = s.indexOf('\t', start);
		try {
			score = Double.parseDouble(s.substring(start, end));
		} catch (Exception e) {
			score = 0.0;
		}

		start = end + 1;
		end = s.indexOf('\t', start);
		char strand = s.charAt(end - 1);
		// added by scooter willis to deal with glimmer predictions that
		// have the start after the end but is a negative strand
		int locationStart = Integer.parseInt(locStart);
		int locationEnd = Integer.parseInt(locEnd);
		if (locationStart > locationEnd) {
			int temp = locationStart;
			locationStart = locationEnd;
			locationEnd = temp;

		}
		Location location = Location.fromBio(locationStart, locationEnd, strand);

		assert (strand == '-') == location.isNegative();

		int frame;
		start = end + 1;
		end = s.indexOf('\t', start);
		try {
			frame = Integer.parseInt(s.substring(start, end));
		} catch (Exception e) {
			frame = -1;
		}

		// grab everything until end of line (or # comment)
		start = end + 1;
		end = s.indexOf('#', start);
		String attributes = null;
		if (end < 0) {
			attributes = new String(s.substring(start));
		} else {
			attributes = new String(s.substring(start, end));
		}

		return new Feature(seqname, source, type, location, score, frame, attributes);

	}

	public static void main(String args[]) throws Exception {

		FeatureList listGenes = GFF3Reader.read("/Users/Scooter/scripps/dyadic/GlimmerHMM/c1_glimmerhmm.gff");
		System.out.println("Features");
		for (FeatureI feature : listGenes) {
			System.out.println(feature);
		}
//        System.out.println(listGenes);
		// GeneMarkGTF.write( list, args[1] );
	}
}

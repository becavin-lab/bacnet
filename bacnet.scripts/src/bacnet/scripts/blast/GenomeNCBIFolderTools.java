package bacnet.scripts.blast;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import bacnet.datamodel.sequenceNCBI.GenomeNCBITools;
import bacnet.reader.FastaFileReader;
import bacnet.reader.GFFNCBIReader;
import bacnet.reader.TabDelimitedTableReader;
import bacnet.utils.ArrayUtils;
import bacnet.utils.FileUtils;

public class GenomeNCBIFolderTools {

	public static String PATH_GENOMES_LIST = GenomeNCBITools.PATH_NCBI_BacGenome + "List_bacteria_assembly_summary.txt";

	/**
	 * Take an array with a list of strain and ftp folders, and: <fi> download them:
	 * fna and gff file only <fi> create a folder for each <fi> verify MD5 checksum
	 * <fi> unzip files
	 * 
	 * The strain name should be in the column with the name: strain_name The folder
	 * name for the genome to be donwload in should be in the column with the name:
	 * folder_name The ftp path should be found in the column with name : ftp_path
	 * The id of the assembly should be found in the column with name :
	 * assembly_accession
	 * 
	 * @param tableGenomes
	 * @param pathGenomes
	 */
	public static void downloadAllGenomes(String tableGenomes, String pathGenomes) {

		String[][] newGenomes = TabDelimitedTableReader.read(tableGenomes);
		for (int i = 1; i < newGenomes.length; i++) {
			String strain = newGenomes[i][ArrayUtils.findColumn(newGenomes, "strain_name")];
			String ftp = newGenomes[i][ArrayUtils.findColumn(newGenomes, "ftp_path")];
			String folderName = newGenomes[i][ArrayUtils.findColumn(newGenomes, "folder_name")];
			String genomeID = newGenomes[i][ArrayUtils.findColumn(newGenomes, "assembly_accession")];

			System.out.println(strain);
//			System.out.println(ftp);
//			System.out.println(genomeID);

			/*
			 * Create folder
			 */
			String path = pathGenomes + "/" + folderName + "/";
			File file = new File(path);
			file.mkdir();
			// System.out.println(path);
			if (file.exists()) {
				// System.out.println("created");
			} else {
				System.err.println("Cannot create:" + path);
			}

			/*
			 * create list of file to download
			 */
			String folderFTP = FileUtils.removePath(ftp);
			// System.out.println(folderFTP);
			downloadAndUnzip(folderFTP, path, ftp, genomeID);

			/*
			 * MD5 Checksum
			 */
			// verifyMD5checksum(path,folderFTP);

		}

	}

	/**
	 * Giving a folderFTP, it will download and unzip:
	 * folderFTP+"_assembly_report.txt" folderFTP+"_assembly_stats.txt"
	 * folderFTP+"_genomic.fna.gz" folderFTP+"_genomic.gff.gz" "md5checksums.txt"
	 * 
	 * @param folderFTP
	 * @param path
	 * @param ftp
	 * @param genomeID
	 */
	private static void downloadAndUnzip(String folderFTP, String path, String ftp, String genomeID) {
		ArrayList<String> downloadFile = new ArrayList<>();
		downloadFile.add("md5checksums.txt");
		downloadFile.add(folderFTP + "_assembly_report.txt");
		downloadFile.add(folderFTP + "_assembly_stats.txt");
		downloadFile.add(folderFTP + "_genomic.fna.gz");
		downloadFile.add(folderFTP + "_genomic.gff.gz");
		downloadFile.add(folderFTP + "_protein.faa.gz");

		/*
		 * Donwload file
		 */
		for (String fileName : downloadFile) {
			String filePath = path + "/" + fileName;
			try {
				URL idfURL = new URL(ftp + "/" + fileName);
				System.out.println("Download: " + idfURL + " to " + filePath);
				org.apache.commons.io.FileUtils.copyURLToFile(idfURL, new File(filePath));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/*
		 * Unzip files
		 */
		String namefnagz = path + "/" + folderFTP + "_genomic.fna.gz";
		String newfna = path + "/" + genomeID + ".fna";
		String namegffgz = path + "/" + folderFTP + "_genomic.gff.gz";
		String newgff = path + "/" + genomeID + ".gff";
		String namefaagz = path + "/" + folderFTP + "_protein.faa.gz";
		String newfaa = path + "/" + genomeID + ".faa";
		try {
			FileUtils.extractGZIP(namefnagz, newfna);
			FileUtils.extractGZIP(namegffgz, newgff);
			FileUtils.extractGZIP(namefaagz, newfaa);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Verify md5 of everything just downloaded
	 * @param path
	 * @param folderFTP
	 */
	@SuppressWarnings("unused")
	private static void verifyMD5checksum(String path, String folderFTP) {
		/*
		 * MD5 Checksum
		 */
		String[][] md5sums = TabDelimitedTableReader.read(new File(path + "md5checksums.txt"), " ");
		HashMap<String, String> fileToMD5 = new HashMap<>();
		for (int j = 0; j < md5sums.length; j++) {
			fileToMD5.put(md5sums[j][2], md5sums[j][0]);
		}
		try {
			FileInputStream fis = new FileInputStream(new File(path + folderFTP + "_genomic.fna.gz"));
			String realmd5fna = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
			fis.close();
			FileInputStream fis2 = new FileInputStream(new File(path + folderFTP + "_genomic.gff.gz"));
			String realmd5gff = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis2);
			fis2.close();
			FileInputStream fis3 = new FileInputStream(new File(path + folderFTP + "_protein.faa.gz"));
			String realmd5faa = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis3);
			fis2.close();
			String namefna = "./" + folderFTP + "_genomic.fna.gz";
			String md5fna = fileToMD5.get(namefna);
			String namegff = "./" + folderFTP + "_genomic.gff.gz";
			String md5gff = fileToMD5.get(namegff);
			String namefaa = "./" + folderFTP + "_protein.faa.gz";
			String md5faa = fileToMD5.get(namefaa);
//			System.out.println(path);
//			System.out.println(realmd5fna+" "+md5fna+" "+namefna);
//			System.out.println(realmd5gff+" "+md5gff+" "+namegff);
//			System.out.println(realmd5faa+" "+md5faa+" "+namefaa);
			if (!realmd5fna.equals(md5fna)) {
				System.err.println("Not equal:" + namefna);
			}
			if (!realmd5gff.equals(md5gff)) {
				System.err.println("Not equal:" + namegff);
			}
			if (!realmd5faa.equals(md5faa)) {
				System.err.println("Not equal:" + namefaa);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * In NCBI genome folder remove everything which is not useful for my
	 * analysis<br>
	 * keep fasta file, .ptt .gff and blastDB files
	 */
	public static void removeUnusedFile() {
		File file = new File(GenomeNCBITools.PATH_NCBI_BacGenome);
		for (File genomeFolder : file.listFiles()) {
			if (genomeFolder.isDirectory()) {
				System.out.println(genomeFolder.getAbsolutePath());
				for (File fileTemp : genomeFolder.listFiles()) {
					String extension = FileUtils.getExtension(fileTemp.getAbsolutePath());
					if (extension.equals(".asn"))
						fileTemp.delete();
					else if (extension.equals(".gbk"))
						fileTemp.delete();
					else if (extension.equals(".tab"))
						fileTemp.delete();
					else if (extension.equals(".rpt"))
						fileTemp.delete();
					else if (extension.equals(".gbs"))
						fileTemp.delete();
					else if (extension.equals(".gbk"))
						fileTemp.delete();
					else if (extension.equals(".Glimmer3"))
						fileTemp.delete();
					else if (extension.equals(".val"))
						fileTemp.delete();
					else if (extension.equals(".rnt"))
						fileTemp.delete();

					if (fileTemp.getAbsolutePath().contains(".GeneMark"))
						fileTemp.delete();
					if (fileTemp.getAbsolutePath().contains(".Prodigal-2.50"))
						fileTemp.delete();
				}
			}
		}
	}

	/**
	 * Go through all folders of Genome directory and extract all TGZ contained in
	 * .faa .fna .ffn .frn .gff
	 * 
	 */
	public static void extractAllContigs(boolean renew) {
		// get all Genome folder
		String[] genomes = GenomeNCBITools.getAvailableGenome();

		for (String geno : genomes) {
			String folder = GenomeNCBITools.getPATH() + geno;
			File fileFolder = new File(folder);
			System.out.println(fileFolder.isDirectory() + " " + folder);
			File[] filesTGZ = fileFolder.listFiles(new FileFilter() {
				@Override
				public boolean accept(File arg0) {
					if (arg0.getAbsolutePath().endsWith(".tgz"))
						return true;
					else
						return false;
				}
			});
			File[] filesFNA = fileFolder.listFiles(new FileFilter() {
				@Override
				public boolean accept(File arg0) {
					if (arg0.getAbsolutePath().endsWith(".fna"))
						return true;
					else
						return false;
				}
			});
			// test if it has been already uncompress by looking if fna file already exists
			boolean alreadyUncompress = false;
			if (filesFNA.length != 0)
				alreadyUncompress = true;

			for (File file : filesTGZ) {
				System.out.println(file.getAbsolutePath());
				String name = file.getAbsolutePath();
				boolean uncompress = false;
				// test if it is full of fasta file
				for (String extension : FastaFileReader.allfileExt) {
					if (name.contains(extension))
						uncompress = true;
				}
				// test if it is a gff file
				if (name.contains(GFFNCBIReader.EXTENSION))
					uncompress = true;

				// if it is already uncompress and we don't want to renew it, we do nothing
				// (uncompress=false)
				if (alreadyUncompress && !renew)
					uncompress = false;

				if (uncompress) {
					System.out.println("Uncompress  " + file.getAbsolutePath());
//					try {
//
//						String source = file.getAbsolutePath();
//						String target = Project.getTEMP_PATH()+"outfile.tar";
//
////						FileUtils.extractGZIP(source, target);
////						bacnet.scripts.core.FileUtilsScripts.extractTAR(target, fileFolder.getAbsolutePath());
//
//					}catch (FileNotFoundException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
				}

			}

		}
	}

}

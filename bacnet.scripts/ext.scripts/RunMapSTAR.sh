#!/bin/bash

# Job variables
JOB_NAME=mapping
NB_CPUS=3
MEM_JOB=30G
home="/home/becavin"
exp_design=$1

# # prepare qsub variables
if [ ! -d $home/Yersiniomics/temp/${JOB_NAME} ]; then
 	mkdir $home/Yersiniomics/temp/${JOB_NAME}
fi

intro_scriptSH="""#!/bin/sh -l
# # activate my environment
PATH="/home/becavin/opt/AlienTrimmer_0.4.0/src/:$PATH"
PATH="/share/apps/local/subread-1.6.4-Linux-x86_64/bin:$PATH"
PATH="$PATH:/home/becavin/opt/STAR-2.7.1a/bin/Linux_x86_64_static/"
"""

folderData=/share/data/users_data/becavin/RNASeq/

while IFS= read -r line; do
    echo "exp design line: $line"
    # script variables
	dataName=$(echo "$line" | awk '{print $1}')
	#folder=$(echo "$line" | awk '{print $2}')
	sra=$(echo "$line" | awk '{print $2}')
	arrayexpress=$(echo "$line" | awk '{print $3}')

	# genome variable
	genomeFolder=$home/Yersiniomics/Genome/
	genome="Yersinia_enterocolitica_subsp_palearctica_Y11.fna"
	genomeSTAR=$home/Yersiniomics/Genome/
	annotation=$genomeFolder'/Yersinia_enterocolitica_subsp_palearctica_Y11.gff'
	
########## run script
	#dataName="scnucseq_brain_human_dropseq"

    # 	echo $
 	shFile=$home/Yersiniomics/temp/${JOB_NAME}/${dataName}_${JOB_NAME}.sh
 	logFile=$home/Yersiniomics/temp/${JOB_NAME}/${dataName}_${JOB_NAME}.log
 	errorFile=$home/Yersiniomics/temp/${JOB_NAME}/${dataName}_${JOB_NAME}_err.log

 	# read files
 	gzfile1=$folderData/${sra}_1.fastq.gz
 	gzfile2=$folderData/${sra}_2.fastq.gz
 	readFile1=$folderData/${dataName}_1
	readFile2=$folderData/${dataName}_2
 	
 	mappingFolder=$folderData/${dataName}
	wigfile1=$folderData/${dataName}/${dataName}Signal.Unique.str1.out
	wigfile2=$folderData/${dataName}/${dataName}Signal.Unique.str2.out
	bamFileUnfiltered=$folderData/${dataName}/${dataName}Aligned.sortedByCoord.out.bam
	bamFileFiltered=$folderData/${dataName}
	outputCount=$folderData/FCount_${dataName}
	
	logfile=$folderData/${dataName}/${dataName}Log.final.out
	newlogfile=$folderData/${dataName}.log

	if [ ! -d $mappingFolder ]; then
	 	mkdir $mappingFolder
	fi

 	scriptSH1=$intro_scriptSH"""
echo $dataName
echo \"Map fastq\"

# mv ${gzfile1} ${readFile1}_raw.fastq.gz
# mv ${gzfile2} ${readFile2}_raw.fastq.gz

# zcat -d ${readFile1}_raw.fastq.gz > ${readFile1}_raw.fastq
# zcat -d ${readFile2}_raw.fastq.gz > ${readFile2}_raw.fastq

#java -jar /home/becavin/opt/AlienTrimmer_0.4.0/src/AlienTrimmer.jar -if ${readFile1}_raw.fastq -ir ${readFile2}_raw.fastq -c $genomeFolder/alienTrimmerPF8contaminants.fasta -of ${readFile1}.fastq -or ${readFile2}.fastq                               
# rm ${readFile1}_raw.fastq
# rm ${readFile2}_raw.fastq

# # Create genome
#STAR --runThreadN ${NB_CPUS} --genomeSAindexNbases 10 --runMode genomeGenerate --genomeDir $genomeSTAR --genomeFastaFiles $genomeFolder/$genome
STAR --version
# #Map dataset
# # # Run basic STAR with normalized WIG
STAR --runThreadN ${NB_CPUS} --genomeDir ${genomeSTAR}  --limitBAMsortRAM 5026551546 --outFileNamePrefix ${mappingFolder}/${dataName} \
--readFilesIn ${readFile1}.fastq ${readFile2}.fastq \
--outSAMtype BAM SortedByCoordinate --outWigType wiggle \
--outWigStrand Stranded --outWigNorm=RPM

mv ${wigfile1}.wig $folderData/${dataName}_f.wig
mv ${wigfile2}.wig $folderData/${dataName}_r.wig

# cp $logfile $newlogfile

# # # # filter bam files by removing data with score = 0
# samtools view -b -q 1 ${bamFileUnfiltered} > ${bamFileFiltered}.bam
# samtools index ${bamFileFiltered}.bam

# #echo \"Count reads per peak\"
#featureCounts -p -M -B -O --verbose -T $NB_CPUS -s 0 -t gene -g Name -a ${annotation} -o ${outputCount}.txt ${bamFileFiltered}.bam

echo \"Finished Mapping\""""
	# save script and run with qsub
	echo """$scriptSH1""" > ${shFile}
	# Run script
	#sh $shFile
	qsub -l ncpus=$NB_CPUS,mem=$MEM_JOB -N $JOB_NAME -e $errorFile -o $logFile $shFile

done < "$exp_design"


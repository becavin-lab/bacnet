#!/bin/bash
#PBS -N BlastHomolog

# Job variables
JOB_NAME=BlastHomolog
NB_CPUS=1
MEM_JOB=5G
home="/home/becavin"

# # prepare qsub variables
if [ ! -d $home/temp/${JOB_NAME} ]; then
 	mkdir $home/temp/${JOB_NAME}
fi

# create result folder
if [ ! -d $home/Yersiniomics/BLASTDB/Results ]; then
 	mkdir $home/Yersiniomics/BLASTDB/Results
fi

scripts=$home/Yersiniomics/listScript.txt
#echo $scripts
echo ${PBS_ARRAYID}
# script variables

shFile=$(head -n ${PBS_ARRAYID} ${scripts} | tail -n 1)

logFile=${home}/temp/${JOB_NAME}/${shFile}.log
errFile=${home}/temp/${JOB_NAME}/${shFile}.err

# Run script
#qsub -l ncpus=$NB_CPUS,mem=$MEM_JOB -N $JOB_NAME -e $errFile -o $logFile $home/Yersiniomics/Threads/Commands/$shFile
sh $home/Yersiniomics/Threads/Commands/$shFile




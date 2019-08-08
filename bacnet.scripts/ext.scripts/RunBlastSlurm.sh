#!/bin/bash
#SBATCH --job-name Blast
#SBATCH --output temp/Blast.out
#SBATCH --error temp/Blast.err
#SBATCH --mail-type FAIL
#SBATCH --qos hubbioit
#SBATCH --cpus-per-task 1
#SBATCH --mem=5GB
#SBATCH -p hubbioit

# Run with sbatch command
# sbatch --array=1:100 listScript.txt 

folder=/pasteur/homes/cbecavin/Yersiniomics/

script=$(head -n ${SLURM_ARRAY_TASK_ID} $1 | tail -n 1)

# prepare variables
if [ ! -d $folder/temp/${SLURM_JOB_NAME} ]; then
	mkdir $folder/temp/${SLURM_JOB_NAME}
fi
shFile=$folder/temp/${SLURM_JOB_NAME}/${SLURM_ARRAY_TASK_ID}_${SLURM_JOB_NAME}_${SLURM_JOB_ID}.sh
logFile=$folder/temp/${SLURM_JOB_NAME}/${SLURM_ARRAY_TASK_ID}_${SLURM_JOB_NAME}_${SLURM_JOB_ID}.log

# Create script SH to run in qsub
scriptSH="""#!/bin/bash -l
module add blast+
sh $folder/Scripts/$script
"""

# save script and run with qsub
echo """$scriptSH""" > ${shFile}
srun -c ${SLURM_CPUS_PER_TASK} -o ${logFile} bash ${shFile}

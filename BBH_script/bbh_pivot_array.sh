#!/bin/bash

#ARGUMENTS
# 1. DB -> folder avec les fichiers proteomes en format fasta et nommés en *.prt
# 2. Fichier avec la liste des références que tu veux tester

#SBATCH --job-name bbh_batch
#SBATCH --output bbh_batch_al%A%a.out
#SBATCH --mail-type FAIL
#SBATCH --mem-per-cpu 5000

module load blast+/2.2.31

FILE=$(head -n ${SLURM_ARRAY_TASK_ID} $2 | tail -n 1)

DB="$1"
PIVOT="$DB/$FILE"
GENOME=${FILE%.*}
OUTFILE="${GENOME}.bbh.txt"
THREADS=${SLURM_CPUS_PER_TASK}

srun bash -c '>"$0"' "$OUTFILE"

srun makeblastdb -in "$PIVOT" -dbtype prot -input_type fasta -title "${GENOME}_pivot" -hash_index -out "${GENOME}_pivot"

for f in "$DB"/*.prt; do
	if [ "$f" != "$PIVOT" ];then
		srun echo "$f"
		srun makeblastdb -in "$f" -dbtype prot -input_type fasta -title "${GENOME}_target" -hash_index -out "${GENOME}_target"
		srun -c "$THREADS" blastp -query "$PIVOT" -out "${GENOME}_query.out" -db "${GENOME}_target" -max_target_seqs 1 -num_threads "$THREADS" -outfmt '6 qseqid sseqid qlen slen length nident positive evalue bitscore'
		srun bash -c 'cat "$0" >> "$1"' "${GENOME}_query.out" "$OUTFILE"
		srun -c "$THREADS" blastp -query "$f" -out "${GENOME}_query.out" -db "${GENOME}_pivot" -max_target_seqs 1 -num_threads "$THREADS" -outfmt '6 qseqid sseqid qlen slen length nident positive evalue bitscore'
		srun bash -c 'cat "$0" >> "$1"' "${GENOME}_query.out" "$OUTFILE"
	fi
done

srun rm "${GENOME}_target".*
srun rm "${GENOME}_query.out"
srun rm "${GENOME}_pivot".*
#!/bin/bash

#############################################################################################################
#                                                                                                           #
# JolyTree: fast distance-based phylogenetic inference from unaligned genome sequences                      #
#                                                                                                           #
# Copyright (C) 2017,2018,2019  Alexis Criscuolo                                                            #
#                                                                                                           #
# This program  is free software:  you can  redistribute it  and/or modify it  under the terms  of the GNU  #
# General Public License as published by the Free Software Foundation, either version 3 of the License, or  #
# (at your option) any later version.                                                                       #
#                                                                                                           #
# This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY;  without even  #
# the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public  #
# License for more details.                                                                                 #
#                                                                                                           #
# You should have received a copy of the  GNU General Public License along with this program.  If not, see  #
# <http://www.gnu.org/licenses/>.                                                                           #
#                                                                                                           #
#  Contact:                                                                                                 #
#  Institut Pasteur                                                                                         #
#  Bioinformatics and Biostatistics Hub                                                                     #
#  C3BI, USR 3756 IP CNRS                                                                                   #
#  Paris, FRANCE                                                                                            #
#                                                                                                           #
#  alexis.criscuolo@pasteur.fr                                                                              #
#                                                                                                           #
#############################################################################################################

#############################################################################################################
#                                                                                                           #
# ============                                                                                              #
# = VERSIONS =                                                                                              #
# ============                                                                                              #
#                                                                                                           #
  VERSION=1.1.181205ac                                                                                      #
# + option -q to set desired probability of observing a random k-mer                                        #
# + ability to be run on clusters managed by SLURM                                                          #
#                                                                                                           #
# VERSION=1.0.180115ac                                                                                      #
# + option -n to only estimate evolutionary distances                                                       #
# + option -r to set the number of iterations when performing the ratchet-based BME tree search             #
# + important bug fixed when sorting the input file names                                                   #
# + reimplementation of the F81 distance estimation                                                         #
#                                                                                                           #
# VERSION=0.8.171207ac                                                                                      #
# + k-mer size could be set by the user                                                                     #
# + bug fixed for manual tbl estimation                                                                     #
# + .fas allowed inside the input directory                                                                 #
#                                                                                                           #
# VERSION=0.7.170919ac                                                                                      #
# + tree output file suffix is now .nwk                                                                     #
#                                                                                                           #
# VERSION=0.6.170728ac                                                                                      #
# + implements the F81  transformation suggested  by Tamura & Kumar (2002)  in order to deal with putative  #
#   heterogeneous substitution pattern among lineages                                                       #
#                                                                                                           #
# VERSION=0.5.170727ac                                                                                      #
# + automatic estimation of the k-mer size                                                                  #
#                                                                                                           #
# VERSION=0.4.170726ac                                                                                      #
# + precomputed pairwise p-distances could be used (option -d)                                              #
# + no limit with the length of the input FASTA filenames                                                   #
#                                                                                                           #
# VERSION=0.3.170724ac                                                                                      #
# + implements the F81 transformation when at least one p-distance is larger than a specified cutoff        #
#                                                                                                           #
# VERSION=0.2.170721ac                                                                                      #
# + uses FastME 2.1.5.1                                                                                     #
#                                                                                                           #
#############################################################################################################

#############################################################################################################
#                                                                                                           #
# ============                                                                                              #
# = DOC      =                                                                                              #
# ============                                                                                              #
#                                                                                                           #
  if [ "$1" = "-?" ] || [ "$1" = "-h" ] || [ $# -le 1 ]                                                     #
  then                                                                                                      #
    cat << EOF                                                       

 JolyTree v.$VERSION

 USAGE:
    JolyTree.sh  [options]
 where:
    -i <directory>  directory name containing  FASTA-formatted contig files;  only files
                    ending with .fa, .fna, .fas or .fasta will be considered (mandatory)
    -b <basename>   basename of every written output file (mandatory)
    -s <int>        sketch size (default: 25% of the largest genome size)
    -q <double>     probability of observing a random k-mer (default: 0.0001)
    -k <int>        k-mer size (default: estimated from the average genome size with the
                    probability set by option -q)
    -c <real>       if at least one of the estimated p-distances is above this specified
                    cutoff, then a F81 correction is performed (default: 0.1)
    -n              no BME tree inference (only pairwise distance estimation)
    -r <int>        number of steps  when performing the  ratchet-based  BME tree search
                    (default: 100)
    -t <int>        number of threads (default: 2)

EOF
    exit 1 ;                                                                                                #
  fi                                                                                                        #
#                                                                                                           #
#############################################################################################################

  
#############################################################################################################
#                                                                                                           #
# ================                                                                                          #
# = INSTALLATION =                                                                                          #
# ================                                                                                          #
#                                                                                                           #
# [1] REQUIREMENTS =======================================================================================  #
#  JolyTree depends on Mash, gawk, FastME and REQ (see below),  each with a minimum version required. You   #
#  should have them installed on your computer prior to using JolyTree.  Make sure that each is installed   #
#  on your $PATH variable, or specify below the full path to each of them.                                  #
#                                                                                                           #
# -- Mash: fast pairwise p-distance estimation --------------------------------------------------------     #
#    VERSION >= 1.0.2                                                                                       #
#    src: github.com/marbl/Mash                                                                             #
#    Ondov BD, Treangen TJ, Melsted P, Mallonee AB, Bergman NH, Koren S, Phillippy AM (2016) Mash: fast     #
#      genome  and  metagenome  distance  estimation  using  MinHash.   Genome  Biology,  17:132.  doi:     #
#      10.1186/s13059-016-0997-x                                                                            #
#                                                                 ################################################
                                                                  ################################################
  MASH=mash;                                                      ## <=== WRITE HERE THE PATH TO THE MASH       ##
                                                                  ##      BINARY (VERSION 1.0.2 MINIMUM)        ##
                                                                  ################################################
                                                                  ################################################
#                                                                                                           #
# -- gawk: fast text file processing ------------------------------------------------------------------     #
#    VERSION >= 4.1.0                                                                                       #
#    src: ftp.gnu.org/gnu/gawk                                                                              #
#    Robbins AD  (2018)  GAWK:  Effective AWK Programming -- A User’s Guide  for GNU Awk  (Edition 4.2)     #
#      www.gnu.org/software/gawk/manual                                                                     #
#                                                                 ################################################
                                                                  ################################################
  GAWK=gawk;                                                      ## <=== WRITE HERE THE PATH TO THE GAWK       ##
                                                                  ##      BINARY (VERSION 4.1.0 MINIMUM)        ##
                                                                  ################################################
                                                                  ################################################
#                                                                                                           #
# -- FastME: fast distance-based phylogenetic tree inference ------------------------------------------     #
#    VERSION >= 2.1.5.1                                                                                     #
#    src: gite.lirmm.fr/atgc/FastME/                                                                        #
#    Lefort V, Desper R, Gascuel O  (2015)  FastME 2.0:  a comprehensive, accurate,  and fast distance-     #
#      based  phylogeny  inference  program.  Molecular Biology and Evolution,  32(10):2798–2800.  doi:     #
#      10.1093/molbev/msv150                                                                                #
#                                                                 ################################################
                                                                  ################################################
  FASTME=fastme;                                                  ## <=== WRITE HERE THE PATH TO THE FASTME     ##
                                                                  ##      BINARY (VERSION 2.1.5.1 MINIMUM)      ##
                                                                  ################################################
                                                                  ################################################
#                                                                                                           #
# -- REQ: fast computation of the rates of elementary quartets ----------------------------------------     #
#    VERSION >= 1.2                                                                                         #
#    src: gitlab.pasteur.fr/GIPhy/REQ                                                                       #
#    Guenoche A, Garreta H (2001) Can we have confidence in a tree representation. In: Gascuel O, Sagot     #
#      MF (eds) Computational Biology.  Lecture Notes in Computer Science, vol 2066.  Springer, Berlin,     #
#      Heidelberg. doi:10.1007/3-540-45727-5_5                                                              #
#                                                                 ################################################
                                                                  ################################################
  REQ=REQ;                                                        ## <=== WRITE HERE THE PATH TO THE REQ        ##
                                                                  ##      BINARY (VERSION 1.2 MINIMUM)          ##
                                                                  ################################################
                                                                  ################################################
#                                                                                                           #
#                                                                                                           #
#                                                                                                           #
# [2] EXECUTE PERMISSION =================================================================================  #
#  In order to run JolyTree, give the execute permission on the script JolyTree.sh:                         #
#    chmod +x JolyTree.sh                                                                                   #
#                                                                                                           #
#                                                                                                           #
#                                                                                                           #
# [3] NOTES ON THE USE OF JOLYTREE WITH SLURM (slurm.schedmd.com) ========================================  #
#  By default, JolyTree is able to perform the pairwise p-distance estimate step on multiple threads (the   #
#  option -t  allows the  number of  threads  to  be specified).  The corresponding  pieces of  codes are   #
#  therefore executed concurrently via the following standard 'launcher':                                   #
#                                                                                                           #
   EXEC="sh -c";                                                                                            #
#                                                                                                           #
#  It is therefore possible to use JolyTree on a computer that allows multiple threads to be executed. It   #
#  is  also possible  to launch  JolyTree on  multiple  threads  on a  cluster managed  by Slurm  via the   #
#  following command line models (with t = number of threads):                                              #
#    srun   <Slurm options> -c $t  ./JolyTree.sh  <JolyTree options>  -t $t                                 #
#    sbatch <Slurm options> -c $t  ./JolyTree.sh  <JolyTree options>  -t $t                                 #
#  Moreover, it is also possible to launch JolyTree on  multiple cores on a cluster managed by Slurm. For   #
#  this particular case, you should first uncomment the following line:                                     #
#                                                                                                           #
#  EXEC="srun -n 1 -N 1 $EXEC";                                                                             #
#                                                                                                           #
#  and launch JolyTree via the following command line models (with t = number of cores):                    #
#    salloc <Slurm options> -n $t  ./JolyTree.sh  <JolyTree options>  -t $t                                 #
#    sbatch <Slurm options> -n $t  ./JolyTree.sh  <JolyTree options>  -t $t                                 #
#                                                                                                           #
#############################################################################################################

  
#############################################################################################################
#############################################################################################################
#### INITIALIZING PARAMETERS AND READING OPTIONS                                                         ####
#############################################################################################################
#############################################################################################################

if [ ! $(command -v $MASH) ];   then echo "$MASH not found"   >&2 ; exit 1 ; fi
if [ ! $(command -v $GAWK) ];   then echo "$GAWK not found"   >&2 ; exit 1 ; fi
if [ ! $(command -v $FASTME) ]; then echo "$FASTME not found" >&2 ; exit 1 ; fi
if [ ! $(command -v $REQ) ];    then echo "$REQ not found"    >&2 ; exit 1 ; fi

DATADIR="N.O.D.I.R";            # -i (mandatory)
BASEFILE="N.O.B.A.S.E.F.I.L.E"; # -b (mandatory)

SKETCH=0;                       # -s (auto from data)
Q=0.00001;                      # -q (0.00001)
K=0;                            # -k (auto from -q)
CUTOFF=0.1;                     # -c (0.1)

INFERTREE=true;                 # -n (none)
RATCHET=100;                    # -r (100)
RATCHET_LIMIT=200;              #    (static)

NPROC=2;                        # -t (2)
WAITIME=0.5;                    #    (auto from -t)

while getopts :i:b:s:q:k:c:d:r:t:n option
do
  case $option in
    i) DATADIR="$OPTARG"                                  ;;
    b) BASEFILE="$OPTARG"                                 ;;
    s) SKETCH=$OPTARG                                     ;;
    q) Q=$OPTARG                                          ;;
    k) K=$OPTARG                                          ;;
    c) CUTOFF=$OPTARG                                     ;;
    n) INFERTREE=false                                    ;;
    r) RATCHET=$OPTARG                                    ;;
    t) NPROC=$OPTARG                                      ;;
    :) echo "option $OPTARG : missing argument" ; exit 1  ;;
   \?) echo "$OPTARG : option invalide" ;         exit 1  ;;
  esac
done
if [ "$DATADIR" == "N.O.D.I.R" ];             then echo "genome directory is not specified (option -i)" >&2 ; exit 1 ; fi
if [ ! -e "$DATADIR" ];                       then echo "genome directory does not exist (option -i)"   >&2 ; exit 1 ; fi
if [ ! -d "$DATADIR" ];                       then echo "$DATADIR is not a directory (option -i)"       >&2 ; exit 1 ; fi
if [ "$BASEFILE" == "N.O.B.A.S.E.F.I.L.E" ];  then echo "basename is not specified (option -b)"         >&2 ; exit 1 ; fi
if [ $SKETCH -ne 0 ] && [ $SKETCH -le 1000 ]; then echo "sketch size $SKETCH is too low (option -s)"    >&2 ; exit 1 ; fi

### verifying the number of threads
[ $NPROC -le 0 ] && NPROC=2;
echo "$NPROC thread(s)" ;
WAITIME=$($GAWK -v x=$NPROC 'BEGIN{print 1/sqrt(x)'});

### gathering the genome list
GLIST=$(ls $DATADIR/*.fna $DATADIR/*.fas $DATADIR/*.fa $DATADIR/*.fasta 2> /dev/null | sort);
n=$(echo $GLIST | $GAWK '{print NF}');
if [ $n -lt 4 ]; then echo "directory $DATADIR should contain at least 4 files *.fna, *.fas, *.fasta or *.fa" >&2 ; exit 1 ; fi
echo "$n taxa" ;

### creating output file names
ACGT=$BASEFILE.acgt;    # ACGT content of each input genome
OEPL=$BASEFILE.oepl;    # p-distance estimates in OEPL (One Entry Per Line) format 
DFILE=$BASEFILE.d;      # evolutioanry distances in PHYLIP square format


#############################################################################################################
#############################################################################################################
#### PREPROCESSING GENOMES                                                                               ####
#############################################################################################################
#############################################################################################################

### estimating ACGT content
rm -f $ACGT ; 
for f in $GLIST
do
  echo "parsing $(basename ${f%.*})" >&2 ;
  $EXEC "x=\$($GAWK '! /^>/{i=split(\$0,c,\"\");++i;while(--i>0)w[c[i]]++}END{print w[\"A\"]+w[\"a\"]\" \"w[\"C\"]+w[\"c\"]\" \"w[\"G\"]+w[\"g\"]\" \"w[\"T\"]+w[\"t\"]}' $f); flock -x $ACGT echo \"$(basename $f) \$x\" >> $ACGT;" &
  while [ $(jobs -r | wc -l) -gt $NPROC ]; do sleep $WAITIME ; done
done

wait ;

sort $ACGT > $ACGT.tmp ;
mv $ACGT.tmp $ACGT ;

### estimating k-mer and sketch size
[ $K -le 0 ] && K=$($GAWK -v q=$Q '{n=$2+$3+$4+$5;kc=int(log(n*(1-q)/q)/log(4))+1;k=(kc>k)?kc:k}END{print k}' $ACGT) && [ $K -le 0 ] && k=19;
echo "k-mer size: $K (q=$Q)" ;
[ $SKETCH -le 0 ] && SKETCH=$($GAWK '{s+=$2+$3+$4+$5;n+=4}END{printf("%d\n", 100000*int((s/n)/100000))}' $ACGT) && [ $SKETCH -eq 0 ] && SKETCH=10000;
echo "sketch size: $SKETCH" ;

### sketching genomes
TLIST="" ;
for f in $GLIST
do
  TLIST="$TLIST $(basename ${f%.*})" ;
  echo "sketching $(basename ${f%.*})" >&2 ;
  $EXEC "$MASH sketch -o ${f%.*} -s $SKETCH -k $K $f" &> /dev/null &
  while [ $(jobs -r | wc -l) -gt $NPROC ]; do sleep $WAITIME ; done
done

wait ; 

#############################################################################################################
#############################################################################################################
#### DISTANCE ESTIMATES                                                                                  ####
#############################################################################################################
#############################################################################################################

### estimating and writing pairwise p-distances
echo $TLIST > $OEPL ;
a=($(ls $DATADIR/*.msh | sort));
i=${#a[@]}; 
while [ $((j=--i)) -ge 0 ]
do
  mi=${a[$i]};
  ti=$(basename ${mi%.*});
  while [ $((--j)) -ge 0 ]
  do
    mj=${a[$j]};
    tj=$(basename ${mj%.*});
    echo "estimating p-distance between $ti ($(( $i + 1 ))) and $tj ($(( $j + 1 )))" >&2 ;
    $EXEC "d=\$(timeout 5 $MASH dist -s $SKETCH $mi $mj | $GAWK '{printf(\"%.8f\\n\",\$3)}'); [ -n \"\$d\" ] && flock -x $OEPL echo \"$(( $i + 1 )) $(( $j + 1 )) \$d\" >> $OEPL ;" &
    while [ $(jobs -r | wc -l) -gt $NPROC ]; do sleep $WAITIME ; done
  done
done

wait ;

### verifying every p-distance estimates
$GAWK '(NR==1){n=NF;while((j=++i)<=n)while(--j>0)d[i][j]=d[j][i]=-1;next}
              {d[$1][$2]=(d[$2][$1]=$3)}
       END    {i=0;while((j=++i)<=n)while(--j>0)if(d[i][j]<0||d[j][i]<0)print i"\t"j}' $OEPL |
  while read i j
  do
    let i--;
    mi=${a[$i]};
    ti=$(basename ${mi%.*});
    let j--;
    mj=${a[$j]};
    tj=$(basename ${mj%.*});
    echo "re-estimating p-distance between $ti ($(( $i + 1 ))) and $tj ($(( $j + 1 )))" >&2 ;
    d=$($MASH dist -s $SKETCH $mi $mj | $GAWK '{printf("%.8f\n",$3)}'); flock -x $OEPL echo "$(( $i + 1 )) $(( $j + 1 )) $d" >> $OEPL ;
  done

wait ;

### transforming (if required) p-distances and writing in PHYLIP square format
if [ -n "$($GAWK -v c=$CUTOFF '(NR==1){next}($3>c){print;exit}' $OEPL)" ]
then
  $GAWK -v p=8 'function s(x){return x*x}
                (ARGIND==1)        {++x;sx=$2+$3+$4+$5;a[x]=$2/sx;c[x]=$3/sx;g[x]=$4/sx;t[x]=$5/sx}
                (ARGIND==2&&FNR==1){while(++n<=NF){m=(m>(l=length(lbl[n]=$n)))?m:l;d[n][n]=0}--n;  print(b=" ")n;x=0.5;while((x*=2)<m)b=b""b;  next}
                (ARGIND==2)        {d[$1][$2]=(d[$2][$1]=$3)}
                END                {while(++i<=n){printf substr(lbl[i]b,1,m);ai=a[i];ci=c[i];gi=g[i];ti=t[i];j=0;
                                      while(++j<=n)printf(" %."p"f",((dij=d[i][j])==0)?0:((x=1-dij/(1-ai*a[j]-ci*c[j]-gi*g[j]-ti*t[j]))>0)?((s(ai+a[j])+s(ci+c[j])+s(gi+g[j])+s(ti+t[j]))/4-1)*log(x):1.23456789);
                                      print""}}' $ACGT $OEPL > $DFILE ;
  echo "F81 distances written into $DFILE" ;
else
  $GAWK -v p=8 '(NR==1){while(++n<=NF){m=(m>(l=length(lbl[n]=$n)))?m:l;d[n][n]=0}--n;  print(b=" ")n;x=0.5;while((x*=2)<m)b=b""b;  next}  
                       {d[$1][$2]=(d[$2][$1]=$3)}
                END    {while(++i<=n){printf substr(lbl[i]b,1,m);j=0;while(++j<=n)printf(" %."p"f",d[i][j]);print""}}' $OEPL > $DFILE ;
  echo "p-distances written into $DFILE" ;
fi

### deleting all *.msh files
for f in $DATADIR/*.msh ; do rm -f $f ; done


if ! $INFERTREE ; then exit 0 ; fi


#############################################################################################################
#############################################################################################################
#### BME TREE INFERENCE                                                                                  ####
#############################################################################################################
#############################################################################################################

echo "searching for the BME phylogenetic tree..." ;

TAXFILE=$BASEFILE.tax;
grep -v "^ " $DFILE | $GAWK '{print $1}' > $TAXFILE ;

$GAWK '(NR==1){print;next}{printf"@"(++i)"@";j=1;while(++j<=NF)printf" "$j;print""}' $DFILE > $BASEFILE.dd ;
DFILE=$BASEFILE.dd;

BMETREE=$BASEFILE.nwk;  # BME phylogenetic tree in NEWICK format
OUTTREE=$BASEFILE.tt;
STATFILE=$DFILE""_fastme_stat.txt;

### first BME tree inference
$FASTME -i $DFILE -o $OUTTREE -s -f 12 -T 1 &> /dev/null ;
tblo=$(grep -B1 "Performed" $STATFILE | sed -n 1p | sed 's/.* //g' | sed 's/\.$//g');
[ -z "$tblo" ] && tblo=$(grep -o ":[0-9\.-]*" $OUTTREE | tr -d :- | paste -sd+ | bc -l | sed 's/^\./0./');
echo "  step 0   $tblo" >&2 ;
echo "step 0   tbl=$tblo" ;
cp $OUTTREE $BMETREE;
i=0; while read tax; do let i++; sed -i "s/@$i""@/$tax/" $BMETREE ; done < $TAXFILE ;

### ratchet-based search of the BME tree
ct=0;
for s in $(seq 1 $RATCHET)
do
  ### noising evolutionary distances
  v=0.$s; [ $(echo "$v>=0.7" | bc) -eq 1 ] && v=0$(echo "scale=4;$v*$v" | bc -l);
  $GAWK -v v=$v -v s=$s 'BEGIN  {srand(s)}
                         (NR==1){n=$0;next}  {lbl[++i]=$1;d[i][i]=0;j=0;f=1;while(++f<=i){++j;d[i][j]=(d[j][i]=($f*(1-v)+2*v*$f*rand()))}}
                         END    {print" "n;i=0;while(++i<=n){printf lbl[i];j=0;while(++j<=n){printf(" %.8f",d[i][j])}print""}}' $DFILE > $DFILE.noised ;

  ### using current BME tree as starting tree for a new BME tree search
  $FASTME -i $DFILE.noised -u $OUTTREE        -o $OUTTREE.noised    -nB -s -T 1 &> /dev/null ;
  sed -i 's/:-/:/g' $OUTTREE.noised ;
  $FASTME -i $DFILE        -u $OUTTREE.noised -o $OUTTREE.candidate     -s -T 1 &> /dev/null ;

  tbl=$(grep -B1 "Performed" $STATFILE | sed -n 1p | sed 's/.* //g' | sed 's/\.$//g'); 
  out=" ";
  [ -z "$tbl" ] && tbl=$(grep -o ":[0-9\.-]*" $OUTTREE.candidate | tr -d :- | paste -sd+ | bc | sed 's/^\./0./') && out="+";
  echo -n "$out step $s   $tbl" >&2 ; 
  if [ $(echo "$tbl<$tblo" | bc) -eq 0 ]
  then
    echo >&2 ;
  else
    ct=0;
    tblo=$tbl;
    mv $OUTTREE.candidate $OUTTREE ;
    cp $OUTTREE $BMETREE;
    i=0; while read tax; do let i++; sed -i "s/@$i""@/$tax/" $BMETREE ; done < $TAXFILE ;
    echo " *" >&2 ;
    echo "step $s   tbl=$tbl"; 
  fi

  rm -f $DFILE.noised_fastme_stat.txt $STATFILE $OUTTREE.noised $OUTTREE.candidate $DFILE.noised ;

  if [ $((++ct)) -eq $RATCHET_LIMIT ]; then break; fi
done

### confidence value at every branch
$REQ $BASEFILE.d $BMETREE $OUTTREE ;
mv $OUTTREE $BMETREE ;
echo "BME tree (tbl=$tblo) with branch supports written into $BMETREE" ;
rm -f $DFILE $TAXFILE $OUTTREE ;


exit ;






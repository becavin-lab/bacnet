#!/usr/local/bin/perl -w
################################################
#
#       Author : Christophe Bécavin
#
################################################

use LWP::Simple;

# my $utils = "https://www.ncbi.nlm.nih.gov/entrez/eutils";

# my $db     = ask_user("Database", "Pubmed");
# my $query  = ask_user("Query",    "zanzibar");
# my $report = ask_user("Report",   "abstract");

use LWP::Simple;

my $db1 = 'genome';
my $db2 = 'nuccore';
my $query = 'listeria';
my $filename = 'C:\Users\ipmc\Documents\test3.txt';

#assemble the esearch URL
$base = 'https://eutils.ncbi.nlm.nih.gov/entrez/eutils/';
$url = $base . "esearch.fcgi?db=$db1&term=$query&retmax=1000";
#post the esearch URL
$output = get($url);

#parse IDs retrieved
$finaltable = "";
while ($output =~ /<Id>(\d+?)<\/Id>/sg) {
  $newbase = "https://www.ncbi.nlm.nih.gov/genomes/Genome2BE/genome2srv.cgi?action=download&status=50|40||30|20&report=proks&group=--%20All%20Prokaryotes%20--&subgroup=--%20All%20Prokaryotes%20--&format=&genome_id=";
  $url = $newbase . $1;
  print "Get genome summary for id : $1\n";
  print $url . "\n";
  $outputgenome = get($url);
  $finaltable = $finaltable . $outputgenome;
}
 
open(FH, '>', $filename) or die $!;
 
print FH $finaltable;
 
close(FH);
 
print "Writing to file successfully!\n";


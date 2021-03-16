package awt.svdmds;

import java.awt.Color;
import java.io.Serializable;


public class SVDData implements Serializable{
	
	private static final long serialVersionUID = -6937438834447957728L;

	public String[] bcName;  //  BC: Name of the Bc   Probe: NULL
	public int[] bcId;		//  BC: Index of the Bc   Probe: NULL
	public String[] probeId;  // BC/Probe: probeId
	public Labels labelID = Labels.USE_SEQUENTIAL_ID;
	public Color[] color;    //  BC: Color corresponding to Bc    Probe: Same color
	
	public enum Labels {
		USE_SEQUENTIAL_ID(), USE_SELECTED_ID(), USE_TEXT_ID();
	}
	
	//   Covariance part
	public double[] singularValue;	
	public double[] inertia;
	public double[] singularValueY;	
	public double[] inertiaY;
	
	
	//   Correlation analysis part
	public double[] singularValueXC;	
	public double[] inertiaXC;
	public double[] singularValueYC;	
	public double[] inertiaYC;
	
	//   Correspondence analysis part
	public double[] singularValueXCorr;	
	public double[] inertiaXCorr;
	public double[] singularValueYCorr;	
	public double[] inertiaYCorr;
	
	// Quality Control
	public double[] qualityControl1;
	public double[] qualityControl2;
	
	
	//  Matrix position for probes
	public double[][] Ynew;
	public double[][] YnewC;
	public double[][] YnewCorr;
	
	// Principal Component for bc
	public double[][] principComponentX;
	public double[][] principComponentXC;
	public double[][] principComponentXCorr;
	
	
	public String getInertieAsString()
	{
		String ret = "";
		
		int total=0;
		ret+=" Inertia for arrays in covariance space: ";
		ret+="\n";
		ret += "[";
		for (double element : inertia) {
			int a = (int) Math.round(element*100);
		     total+=a;
		     if(a>0) ret += a+"%,";
		}
		ret = ret.substring(0, ret.length()-1);
		ret += "]";
		ret+=" total of information retrieve: " + total+"%";
		ret+="\n"+"\n";
		
		total=0;
		ret+=" Inertia for probes in covariance space: ";
		ret+="\n";
		ret += "[";
		for (double element : inertiaY) {
			int a = (int) Math.round(element*100);
		     total+=a;
		     if(a>0) ret += a+"%,";
		}
		ret = ret.substring(0, ret.length()-1);
		ret += "]";
		ret+=" total of information retrieve: " + total+"%";
		ret+="\n"+"\n";

		total=0;
		ret+=" Inertia for arrays in correlation space: " + "\n";
		ret += "[";
		for (double element : inertiaXC) {
			int a = (int) Math.round(element*100);
		     total+=a;
		     if(a>0) ret += a+"%,";
		}
		ret = ret.substring(0, ret.length()-1);
		ret += "]";
		ret+=" total of information retrieve: " + total+"%";
		ret+="\n"+"\n";
	
		total=0;
		ret+=" Inertia for probes in correlation space: " + "\n";
		ret += "[";
		for (double element : inertiaYC) {
			int a = (int) Math.round(element*100);
		     total+=a;
		     if(a>0) ret += a+"%,";
		}
		ret = ret.substring(0, ret.length()-1);
		ret += "]";
		ret+=" total of information retrieve: " + total+"%";
		ret+="\n"+"\n";
		
		total=0;
		ret+=" Inertia for biological conditions in correspondence space: " + "\n";
		ret += "[";
		for (double element : inertiaXCorr) {
			int a = (int) Math.round(element*100);
		     total+=a;
		     if(a>0) ret += a+"%,";
		}
		ret = ret.substring(0, ret.length()-1);
		ret += "]";
		ret+=" total of information retrieve: " + total+"%";
		ret+="\n"+"\n";
		
		total=0;
		ret+=" Inertia for probes in correspondence space: " + "\n";
		ret += "[";
		for (double element : inertiaYCorr) {
			int a = (int) Math.round(element*100);
		     total+=a;
		     if(a>0) ret += a+"%,";
		}
		ret = ret.substring(0, ret.length()-1);
		ret += "]";
		ret+=" total of information retrieve: " + total+"%";
	
		return ret;
	}
	
	public String getSingularValueAsString()
	{
		String ret = "";
		ret+="Singular value for arrays in covariance space: " + "\n";
		ret += "[";
		for (double element : singularValue) {
			ret += element+",";
			
		}
		ret = ret.substring(0, ret.length()-1);
		ret += "]";
		ret+="\n";
		
		ret+="Singular value for probes in covariance space: " + "\n";
		ret += "[";
		for (double element : singularValueY) {
			ret += element+",";
			
		}
		ret = ret.substring(0, ret.length()-1);
		ret += "]";
		ret+="\n";
		
		ret+="Singular value for arrays in correlation space: " + "\n";
		ret += "[";
		for (double element : singularValueXC) {
			ret += element+",";
		}
		ret = ret.substring(0, ret.length()-1);
		ret += "]";
		ret += "\n";
		
		ret+="Singular value for probes in correlation space: " + "\n";
		ret += "[";
		for (double element : singularValueYC) {
			ret += element+",";
		}
		ret = ret.substring(0, ret.length()-1);
		ret += "]";
		ret += "\n";
		
		ret+="Singular value for arrays in correspondence space: " + "\n";
		ret += "[";
		for (double element : singularValueXCorr) {
			ret += element+",";
		}
		ret = ret.substring(0, ret.length()-1);
		ret += "]";
		ret += "\n";
		
		ret+="Singular value for probes in correspondence space: " + "\n";
		ret += "[";
		for (double element : singularValueYCorr) {
			ret += element+",";
		}
		ret = ret.substring(0, ret.length()-1);
		ret += "]";
		ret += "\n";
		
		return ret;
	}

	public String getQC1AsString(){
		String ret = "";
		for(double element:qualityControl1){
			ret+=element+", ";
		}
		//ret+= "\n" + " if qc1>1 the algorithm is very good";
		return ret;
	}
	public String getQC2AsString(){
		String ret = "";
		for(double element:qualityControl2){
			ret+=element+", ";
		}
		//ret+="\n" +" if qc2>10^-4 the algorithm is very good";
		return ret;
	}
}
	

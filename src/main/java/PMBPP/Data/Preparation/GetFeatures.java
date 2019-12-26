package PMBPP.Data.Preparation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.json.simple.parser.ParseException;

import PMBPP.Utilities.JSONReader;
import PMBPP.Log.Log;
import PMBPP.ML.Model.Parameters;
import PMBPP.Utilities.CSVReader;
import PMBPP.Utilities.FilesUtilities;

public class GetFeatures {
	public static void main(String[] args) throws IOException, NumberFormatException, IllegalArgumentException, IllegalAccessException, ParseException {
		// TODO Auto-generated method stub
		
		
		for(File m : new FilesUtilities().ReadMtzList("/Volumes/PhDHardDrive/EditorsRevision-2/Datasets/NO-NCS")) {
			Features features=new GetFeatures().Get(m.getAbsolutePath());
			//System.out.println(features.Max);
			//System.out.println(features.RMSD);
			//System.out.println(features.Min);
			//System.out.println(features.Skew);
			//System.out.println(features.Resolution);
		}
		
		
	
	//Features features2 = new Features ();
	//System.out.println(features.isEmpty());
	}
	
	public Features Get(String mtz) throws IOException, NumberFormatException, IllegalArgumentException, IllegalAccessException, ParseException {
		Features features=new Features();
		String mtzName=new File(mtz).getName().replaceAll("."+FilenameUtils.getExtension(new File(mtz).getName()),"");
boolean ReadFromCSV=true;
		if(new File("features.csv").exists()) {
			HashMap<String, Vector<HashMap<String,String>>> map = new CSVReader().ReadIntoHashMap("features.csv", "PDB");
		for(String PDB : map.keySet()) {
			if(PDB.equals(mtzName)) {
				for(int i=0 ; i < map.get(PDB).size(); ++i) {
					for(String Col : map.get(PDB).get(i).keySet()) {
						features.SetFeatureByName(Col, Double.parseDouble(map.get(PDB).get(i).get(Col)));
					}
				}
				break;
			}
		}
		}
		if(!new File("features.csv").exists() || features.isEmpty()) { // empty when the mtz not in the csv file
			ReadFromCSV=false;
			
			 features= new cfft().Cfft(new File(mtz).getAbsolutePath());
			 
			features.Resolution=new mtzinfo().GetReso(new File(mtz).getAbsolutePath());
			
			if(Parameters.MR.equals("T") ) {
				SetSequenceIdentity(features,mtz);
			}
			
		}
		
		if(ReadFromCSV==true)
			new Log().Info(this,"features have read from CSV file: "+mtzName);
		else
			new Log().Info(this,"features have read from cfft: "+mtzName);
		
		return features;
	}
	public Features GetFeaturesFromCSV(String mtz) throws IOException, NumberFormatException, IllegalArgumentException, IllegalAccessException {
		Features features=new Features();
		String MTZ=new File(mtz).getName().replaceAll("."+FilenameUtils.getExtension(new File(mtz).getName()),"");
		HashMap<String, Vector<HashMap<String,String>>> map = new CSVReader().ReadIntoHashMap(MTZ+"features.csv", "PDB");
		for(int i=0 ; i < map.get(MTZ).size(); ++i) {
			for(String Col : map.get(MTZ).get(i).keySet()) {
				features.SetFeatureByName(Col, Double.parseDouble(map.get(MTZ).get(i).get(Col)));
			}
		}
		return features;
	}
	public double [] GetUsingFeatures(String mtz) throws IOException, IllegalArgumentException, IllegalAccessException, ParseException {
		 
		
		 Features features = Get(mtz);
		 if(Parameters.MR.equals("T") ) {
				SetSequenceIdentity(features,mtz);
			}
		//check used features from AttCSV
		updatefromAttCSV();
		
		
		
		return FeaturesToarray(features);
		
	}
	public double [] GetUsingFeatures(String mtz , double Resolution) throws IOException, IllegalArgumentException, IllegalAccessException, ParseException {
		Features features =new cfft().Cfft(new File(mtz).getAbsolutePath());
		features.Resolution=Resolution;
		if(Parameters.MR.equals("T") ) {
			SetSequenceIdentity(features,mtz);
		}
		//check used features from AttCSV
				updatefromAttCSV();
				
				
				
				return FeaturesToarray(features);
	}
	double [] FeaturesToarray(Features features) throws IllegalArgumentException, IllegalAccessException {
		double [] featuresToarray = new double [Parameters.Featuers.split(",").length];
		for(int i=0 ; i < Parameters.Featuers.split(",").length;++i) {
		  
		    if(features.GetFeatureByName(Parameters.Featuers.split(",")[i])!=null)
			featuresToarray[i]=(double)features.GetFeatureByName(Parameters.Featuers.split(",")[i]);
		}
		return featuresToarray;
	}
	
	public LinkedHashMap<String,String>  GetUsingFeaturesInHashMap(String mtz) throws IOException, IllegalArgumentException, IllegalAccessException, ParseException {
		Features features = Get(mtz);
		
		if(Parameters.MR.equals("T") ) {
			SetSequenceIdentity(features,mtz);
		}
		//check used features from AttCSV
		updatefromAttCSV();
		
		LinkedHashMap<String,String>  map = new LinkedHashMap<String,String> ();
		for(int i=0 ; i < Parameters.Featuers.split(",").length;++i) {
		
			if(features.GetFeatureByName(Parameters.Featuers.split(",")[i])!=null)
			map.put(Parameters.Featuers.split(",")[i], String.valueOf(features.GetFeatureByName(Parameters.Featuers.split(",")[i])));
		}
		
		return map;
		
	}
	void updatefromAttCSV() throws IOException {
		BufferedReader att;
		
		 att = new BufferedReader(new FileReader(Parameters.AttCSV));
		
		Parameters.Featuers=att.readLine();
		
		//remove MeasurementUnitsToPredict from first line in CSV
		String [] features=Parameters.Featuers.split(",");
		Parameters.Featuers="";
		for(String feature :  features) {
			if(!Parameters.MeasurementUnitsToPredict.contains(feature)) {
				Parameters.Featuers+=feature+",";
			}
		}
		Parameters.Featuers=Parameters.Featuers.substring(0,Parameters.Featuers.length()-1);//remove last comma		
		
		att.close();
	}
	
	void SetSequenceIdentity(Features features , String mtz ) throws NumberFormatException, FileNotFoundException, IOException, ParseException {
		String mtzName=new File(mtz).getName().replaceAll("."+FilenameUtils.getExtension(new File(mtz).getName()),"");

		if(Parameters.SequenceIdentity.equals("-1")) {
			if(new File(new File(mtz).getParent()+"/"+mtzName+".json").exists()) {
				features.SequenceIdentity=Double.parseDouble(new PMBPP.Utilities.JSONReader().JSONToHashMap(new File(mtz).getParent()+"/"+mtzName+".json").get("gesamt_seqid"));
			}
			else {
				System.out.println("It seems that you do not type in the sequence identity and no json file was found. Please type in the sequence identity using SequenceIdentity=Val ");
				System.exit(-1);
			}
		}
		else {
			features.SequenceIdentity=Double.parseDouble(Parameters.SequenceIdentity);
		}
		
		
	}
}
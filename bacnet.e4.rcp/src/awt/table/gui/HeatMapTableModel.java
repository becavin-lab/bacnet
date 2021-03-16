package awt.table.gui;

import javax.swing.table.AbstractTableModel;

import bacnet.datamodel.dataset.ExpressionMatrix;
import bacnet.table.core.ColorMapperRCPList;
import bacnet.utils.ArrayUtils;

public class HeatMapTableModel extends AbstractTableModel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4696170648253085971L;
	public static int nAN = -1000000;
	private ColorMapperRCPList colorMapper = new ColorMapperRCPList();
	private ExpressionMatrix matrix;
	public String[] columnNames;
	private String[][] expressionMatrixToDisplay;
	
	private int type = -1;
	
	public void setHeatMapTableModel(ExpressionMatrix matrix,int type){
		this.matrix = matrix;
		String firstRow = matrix.getFirstRowName();
		String secondRow = matrix.getSecondRowName();
		
		expressionMatrixToDisplay = matrix.toArray(firstRow);
		columnNames = ArrayUtils.getRow(expressionMatrixToDisplay, 0);
		expressionMatrixToDisplay = ArrayUtils.deleteRow(expressionMatrixToDisplay, 0);
		
		// create includeRow and includeColumn
		this.type = type;
//		switch(type){
//			case 0 : // display only rowNames
//				includeColumn.add(columnNames[0]);
//				break;
//			case 1 : // display only first column
//				includeColumn.add(columnNames[1]);
//				break;
//			case 2 : // delete rowNames column
//				for(int i=1;i<columnNames.length;i++){
//					includeColumn.add(columnNames[i]);
//				}
//				break;
//			case 3 : // display only first column
//				includeColumn.add(columnNames[1]);
//				break;
//			case 4 : // display only rowNames adding inocua annotation
//				includeColumn.add(columnNames[0]);
//				// change names
//				for(int i=0;i<expressionMatrixToDisplay.length;i++){
//					if(Double.parseDouble(expressionMatrixToDisplay[i][1])==2) expressionMatrixToDisplay[i][0] += "  ";
//					else if(Double.parseDouble(expressionMatrixToDisplay[i][1])==0) expressionMatrixToDisplay[i][0] += "*";
//					else if(Double.parseDouble(expressionMatrixToDisplay[i][1])==1) expressionMatrixToDisplay[i][0] += "#";
//				}
//				break;
//			default :
//				for(int i=0;i<columnNames.length;i++){
//					includeColumn.add(columnNames[i]);
//				}
//				break;
//		}
//		
////		for(int i=expressionMatrixToDisplay.length/2;i<expressionMatrixToDisplay.length;i++){
////			includeRow.add(expressionMatrixToDisplay[i][0]);
////		}
////		for(int i=0;i<expressionMatrixToDisplay.length/2;i++){
////			includeRow.add(expressionMatrixToDisplay[i][0]);
////		}
//		for(int i=0;i<expressionMatrixToDisplay.length;i++){
//			includeRow.add(expressionMatrixToDisplay[i][0]);
//		}
		
		
	}
	
	
	
	@Override
	public int getColumnCount() {
		return expressionMatrixToDisplay[0].length;
	}
	
	@Override
	public String getColumnName(int col) {
          return columnNames[col];
      }

	@Override
	public int getRowCount() {
		return expressionMatrixToDisplay.length;
	}

	@Override
	public Object getValueAt(int arg0, int arg1) {
		try{
		//System.out.println("a0 "+arg0+" a1 "+arg1);
		String text = expressionMatrixToDisplay[arg0][arg1];
		if(getColumnClass(arg1)==String.class) return text;
		else{
			if(text.equals("")) return (double)ExpressionMatrix.MISSING_VALUE;
			else return Double.valueOf(text);
		}
		}catch(ArrayIndexOutOfBoundsException e){
			System.out.println("exception");
			return null;
		}
	}
	
	

	public Class getColumnClass(int c) {
		if(c==0) return String.class;
		if(c<matrix.getHeaders().size()+1) return Double.class;
		else return String.class;
     }
	
	
	/*
	 * *****************************************************
	 * 
	 * 			Getters and setters
	 * 
	 * *****************************************************
	 */
	
	public ColorMapperRCPList getColorMapper() {
		return colorMapper;
	}
	public void setColorMapper(ColorMapperRCPList colorMapper) {
		this.colorMapper = colorMapper;
	}
	
	public ExpressionMatrix getMatrix() {
		return matrix;
	}



	public void setMatrix(ExpressionMatrix matrix) {
		this.matrix = matrix;
	}



	public String[][] getExpressionMatrixToDisplay() {
		return expressionMatrixToDisplay;
	}
	public void setExpressionMatrixToDisplay(String[][] expressionMatrixToDisplay) {
		this.expressionMatrixToDisplay = expressionMatrixToDisplay;
	}
//	public ArrayList<String> getExcludeColumn() {
//		return excludeColumn;
//	}
//	public void setExcludeColumn(ArrayList<String> excludeColumn) {
//		this.excludeColumn = excludeColumn;
//	}
//	public ArrayList<String> getExcludeRow() {
//		return excludeRow;
//	}
//	public void setExcludeRow(ArrayList<String> excludeRow) {
//		this.excludeRow = excludeRow;
//	}
//	public ArrayList<String> getIncludeColumn() {
//		return includeColumn;
//	}
//	public void setIncludeColumn(ArrayList<String> includeColumn) {
//		this.includeColumn = includeColumn;
//	}
//	public ArrayList<String> getIncludeRow() {
//		return includeRow;
//	}
//	public void setIncludeRow(ArrayList<String> includeRow) {
//		this.includeRow = includeRow;
//	}

}
